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
import com.makemission.makeflow.domain.ZiidiMessageParser
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
                val isMpesaMessage = messages.all { MpesaMessageParser.isMpesaSender(it.originatingAddress) }
                val isZiidiMessage = messages.all { ZiidiMessageParser.isZiidiSender(it.originatingAddress) }
                if (!isMpesaMessage && !isZiidiMessage) return@launch

                val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
                val operationId = AppLogger.newOperationId()
                val timestamp = messages.maxOfOrNull { it.timestampMillis }
                AppLogger.event(
                    tag = "MpesaSmsReceiver",
                    event = "message_received",
                    operationId = operationId,
                    details = mapOf(
                        "sender_type" to if (isZiidiMessage) "ZIIDI" else "MPESA",
                        "message_characters" to body.length,
                        "parts" to messages.size
                    )
                )
                val parsed = (
                    ZiidiMessageParser.parse(body, timestamp)
                        ?: MpesaMessageParser.parse(body)?.copy(timestampMillis = timestamp)
                    )
                    ?.takeIf { it.amount > 0.0 && it.type != MpesaMessageParser.TransactionType.UNKNOWN }
                    ?.copy(operationId = operationId)
                if (parsed == null) {
                    AppLogger.event(
                        "MpesaSmsReceiver",
                        "message_not_recognized",
                        operationId,
                        mapOf("sender_type" to if (isZiidiMessage) "ZIIDI" else "MPESA"),
                        "WARN"
                    )
                    return@launch
                }
                AppLogger.event(
                    "MpesaSmsReceiver",
                    "message_parsed",
                    operationId,
                    mapOf(
                        "source" to parsed.balanceSource,
                        "type" to parsed.type,
                        "amount" to parsed.amount,
                        "closing_balance" to parsed.balance,
                        "reference_hash" to AppLogger.hashIdentifier(parsed.code)
                    )
                )

                val transactionDao = AppDatabase.getDatabase(appContext).transactionDao()
                if (transactionDao.hasExternalReference(parsed.code)) {
                    AppLogger.event("MpesaSmsReceiver", "duplicate_skipped", operationId, mapOf("reference_hash" to AppLogger.hashIdentifier(parsed.code)))
                    return@launch
                }
                val feesEnabled = preferences.logMpesaCosts.first()
                if (preferences.mpesaBackgroundLoggingEnabled.first()) {
                    val result = try {
                        MpesaBackgroundImporter.importIfSafe(appContext, parsed, feesEnabled, operationId)
                    } catch (error: Exception) {
                        AppLogger.error("MpesaSmsReceiver", "Could not save an M-PESA transaction in the background", error)
                        AppLogger.event("MpesaSmsReceiver", "background_import_failed", operationId, mapOf("error_type" to error.javaClass.simpleName), "ERROR")
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
                            AppLogger.event("MpesaSmsReceiver", "background_import_complete", operationId, mapOf("result" to "saved", "balance_matches" to result.balanceCheck?.matches))
                        }
                        MpesaBackgroundImporter.Result.AlreadyLogged -> {
                            AppLogger.event("MpesaSmsReceiver", "background_import_complete", operationId, mapOf("result" to "duplicate"))
                            return@launch
                        }
                        MpesaBackgroundImporter.Result.ReviewRequired -> {
                            MpesaAutoImportNotification.show(appContext, parsed)
                            AppLogger.event("MpesaSmsReceiver", "background_import_complete", operationId, mapOf("result" to "review_required"), "WARN")
                        }
                    }
                } else {
                    MpesaAutoImportNotification.show(appContext, parsed)
                    AppLogger.event("MpesaSmsReceiver", "review_notification_shown", operationId, mapOf("background_logging" to false))
                }
            } catch (error: Exception) {
                AppLogger.error("MpesaSmsReceiver", "Could not process an incoming M-PESA message", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
