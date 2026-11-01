package com.makemission.makeflow.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.makemission.makeflow.MainActivity
import com.makemission.makeflow.R
import com.makemission.makeflow.domain.MpesaBalanceCheck
import com.makemission.makeflow.domain.MpesaMessageParser
import com.makemission.makeflow.domain.MpesaZiidiTransferResolver
import java.util.Locale

object MpesaAutoImportNotification {
    const val ACTION_REVIEW = "com.makemission.makeflow.action.REVIEW_MPESA_IMPORT"
    const val ACTION_CATEGORIZE_SAVED = "com.makemission.makeflow.action.CATEGORIZE_SAVED_MPESA_IMPORT"

    private const val EXTRA_CODE = "mpesa_import_code"
    private const val EXTRA_TYPE = "mpesa_import_type"
    private const val EXTRA_AMOUNT = "mpesa_import_amount"
    private const val EXTRA_COUNTERPARTY = "mpesa_import_counterparty"
    private const val EXTRA_DATE = "mpesa_import_date"
    private const val EXTRA_TIME = "mpesa_import_time"
    private const val EXTRA_COST = "mpesa_import_cost"
    private const val EXTRA_HAS_COST = "mpesa_import_has_cost"
    private const val EXTRA_BALANCE = "mpesa_import_balance"
    private const val EXTRA_HAS_BALANCE = "mpesa_import_has_balance"
    private const val EXTRA_BALANCE_SOURCE = "mpesa_import_balance_source"
    private const val EXTRA_TIMESTAMP = "mpesa_import_timestamp"
    private const val EXTRA_OPERATION_ID = "mpesa_import_operation_id"
    private const val EXTRA_TRANSACTION_ID = "mpesa_saved_transaction_id"

    fun hasNotificationAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(NotificationChannels.CHANNEL_MPESA_IMPORTS)?.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }
        return true
    }

    fun show(context: Context, message: MpesaMessageParser.MpesaParsedMessage) {
        if (!hasNotificationAccess(context)) return

        NotificationChannels.createChannels(context)
        val reviewIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_REVIEW
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_CODE, message.code)
            putExtra(EXTRA_TYPE, message.type.name)
            putExtra(EXTRA_AMOUNT, message.amount)
            putExtra(EXTRA_COUNTERPARTY, message.counterpartyName)
            putExtra(EXTRA_DATE, message.date)
            putExtra(EXTRA_TIME, message.time)
            putExtra(EXTRA_HAS_COST, message.transactionCost != null)
            putExtra(EXTRA_COST, message.transactionCost ?: 0.0)
            putExtra(EXTRA_HAS_BALANCE, message.balance != null)
            putExtra(EXTRA_BALANCE, message.balance ?: 0.0)
            putExtra(EXTRA_BALANCE_SOURCE, message.balanceSource.name)
            putExtra(EXTRA_TIMESTAMP, message.timestampMillis ?: 0L)
            putExtra(EXTRA_OPERATION_ID, message.operationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.code.hashCode(),
            reviewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_MPESA_IMPORTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (isZiidiTransfer(message)) "ZiDi transfer ready" else "M-PESA transaction ready")
            .setContentText("Tap to review and save it in MakeFlow")
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(message.code.hashCode(), notification)
    }

    fun showLogged(
        context: Context,
        message: MpesaMessageParser.MpesaParsedMessage,
        transactionId: String,
        balanceCheck: MpesaBalanceCheck?,
        anomalyDetected: Boolean
    ) {
        if (!hasNotificationAccess(context)) return

        NotificationChannels.createChannels(context)
        val openIntent = createCategorizeSavedIntent(context, transactionId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.code.hashCode() xor 0x4D504553,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = when {
            balanceCheck != null && !balanceCheck.matches -> {
                val direction = if (balanceCheck.difference > 0.0) "above" else "below"
                val balanceAccount = if (message.balanceSource == MpesaMessageParser.BalanceSource.ZIIDI) "ZiDi" else "M-PESA"
                "Saved. SMS balance is Ksh ${formatAmount(kotlin.math.abs(balanceCheck.difference))} $direction the app's expected balance. Review your $balanceAccount account."
            }
            anomalyDetected -> "Saved. Review the unusually large amount in MakeFlow."
            else -> if (isZiidiTransfer(message)) {
                "Saved to your ${destinationAccountLabel(message)} account. Tap to review the transfer in MakeFlow."
            } else {
                "Saved to your ${destinationAccountLabel(message)} account. Tap to categorize it in MakeFlow."
            }
        }
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_MPESA_IMPORTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (isZiidiTransfer(message)) "ZiDi transfer saved" else "M-PESA transaction saved")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(message.code.hashCode(), notification)
    }

    fun createCategorizeSavedIntent(context: Context, transactionId: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_CATEGORIZE_SAVED
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_TRANSACTION_ID, transactionId)
        }

    fun readFromIntent(intent: Intent?): MpesaMessageParser.MpesaParsedMessage? {
        if (intent?.action != ACTION_REVIEW) return null
        val code = intent.getStringExtra(EXTRA_CODE)
            ?.takeIf { it.matches(Regex("[A-Z0-9]{4,20}")) }
            ?: return null
        val type = intent.getStringExtra(EXTRA_TYPE)
            ?.let { name -> runCatching { MpesaMessageParser.TransactionType.valueOf(name) }.getOrNull() }
            ?.takeIf { it != MpesaMessageParser.TransactionType.UNKNOWN }
            ?: return null
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
            .takeIf { it.isFinite() && it > 0.0 }
            ?: return null
        val date = intent.getStringExtra(EXTRA_DATE).orEmpty()
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        val hasCost = intent.getBooleanExtra(EXTRA_HAS_COST, false)
        val cost = if (hasCost) {
            intent.getDoubleExtra(EXTRA_COST, 0.0).takeIf { it.isFinite() && it >= 0.0 } ?: return null
        } else {
            null
        }
        val hasBalance = intent.getBooleanExtra(EXTRA_HAS_BALANCE, false)
        val balance = if (hasBalance) {
            intent.getDoubleExtra(EXTRA_BALANCE, 0.0).takeIf { it.isFinite() && it >= 0.0 } ?: return null
        } else {
            null
        }
        val balanceSource = intent.getStringExtra(EXTRA_BALANCE_SOURCE)
            ?.let { name -> runCatching { MpesaMessageParser.BalanceSource.valueOf(name) }.getOrNull() }
            ?: MpesaMessageParser.BalanceSource.MPESA
        val timestampMillis = intent.getLongExtra(EXTRA_TIMESTAMP, 0L).takeIf { it > 0L }

        return MpesaMessageParser.MpesaParsedMessage(
            code = code,
            type = type,
            amount = amount,
            counterpartyName = intent.getStringExtra(EXTRA_COUNTERPARTY),
            counterpartyPhone = null,
            date = date,
            time = time,
            balance = balance,
            transactionCost = cost,
            balanceSource = balanceSource,
            timestampMillis = timestampMillis,
            operationId = intent.getStringExtra(EXTRA_OPERATION_ID)
        )
    }

    fun readSavedTransactionId(intent: Intent?): String? {
        if (intent?.action != ACTION_CATEGORIZE_SAVED) return null
        return intent.getStringExtra(EXTRA_TRANSACTION_ID)
            ?.takeIf { it.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) }
    }

    private fun isZiidiTransfer(message: MpesaMessageParser.MpesaParsedMessage): Boolean =
        MpesaZiidiTransferResolver.isZiidiCounterparty(message.counterpartyName)

    private fun destinationAccountLabel(message: MpesaMessageParser.MpesaParsedMessage): String =
        if (isZiidiTransfer(message) && MpesaZiidiTransferResolver.direction(message.type) == MpesaZiidiTransferResolver.Direction.TO_ZIIDI) {
            "ZiDi"
        } else {
            "M-PESA"
        }

    private fun formatAmount(amount: Double): String =
        String.format(Locale.getDefault(), "%,.2f", amount)
}
