package com.makemission.makeflow.core.sms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.makemission.makeflow.core.logging.AppLogger
import com.makemission.makeflow.core.notifications.MpesaAutoImportNotification
import com.makemission.makeflow.data.local.AppDatabase
import com.makemission.makeflow.data.local.UserPreferencesDataStore
import com.makemission.makeflow.data.local.userPrefDataStore
import com.makemission.makeflow.domain.MpesaMessageParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MpesaSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val preferences = UserPreferencesDataStore(appContext.userPrefDataStore)
                if (!preferences.mpesaAutoImportEnabled.first()) return@launch
                if (ContextCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.RECEIVE_SMS
                    ) != PackageManager.PERMISSION_GRANTED
                ) return@launch
                if (!MpesaAutoImportNotification.hasNotificationAccess(appContext)) return@launch

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNullOrEmpty()) return@launch
                if (messages.any { !MpesaMessageParser.isMpesaSender(it.originatingAddress) }) return@launch

                val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
                val parsed = MpesaMessageParser.parse(body)
                    ?.takeIf { it.amount > 0.0 && it.type != MpesaMessageParser.TransactionType.UNKNOWN }
                    ?: return@launch

                val transactionDao = AppDatabase.getDatabase(appContext).transactionDao()
                if (transactionDao.hasExternalReference(parsed.code)) return@launch
                val feesEnabled = preferences.logMpesaCosts.first()
                if (preferences.mpesaBackgroundLoggingEnabled.first()) {
                    val result = try {
                        MpesaBackgroundImporter.importIfSafe(appContext, parsed, feesEnabled)
                    } catch (error: Exception) {
                        AppLogger.error("MpesaSmsReceiver", "Could not save an M-PESA transaction in the background", error)
                        MpesaBackgroundImporter.Result.ReviewRequired
                    }
                    when (result) {
                        is MpesaBackgroundImporter.Result.Saved -> {
                            com.makemission.makeflow.widget.WidgetUpdateHelper.updateWidgets(appContext)
                            MpesaAutoImportNotification.showLogged(
                                appContext,
                                parsed,
                                result.balanceCheck,
                                result.anomalyDetected
                            )
                            AppLogger.info("MpesaSmsReceiver", "Saved a recognized M-PESA transaction in the background")
                        }
                        MpesaBackgroundImporter.Result.AlreadyLogged -> return@launch
                        MpesaBackgroundImporter.Result.ReviewRequired -> {
                            MpesaAutoImportNotification.show(appContext, parsed)
                            AppLogger.info("MpesaSmsReceiver", "M-PESA transaction needs review before saving")
                        }
                    }
                } else {
                    MpesaAutoImportNotification.show(appContext, parsed)
                    AppLogger.info("MpesaSmsReceiver", "Recognized an M-PESA transaction for review")
                }
            } catch (error: Exception) {
                AppLogger.error("MpesaSmsReceiver", "Could not process an incoming M-PESA message", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
