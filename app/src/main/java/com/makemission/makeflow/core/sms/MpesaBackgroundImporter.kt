package com.makemission.makeflow.core.sms

import android.content.Context
import com.makemission.makeflow.core.haptics.HapticManager
import com.makemission.makeflow.core.haptics.HapticStrength
import com.makemission.makeflow.core.logging.AppLogger
import com.makemission.makeflow.data.local.AppDatabase
import com.makemission.makeflow.data.local.UserPreferencesDataStore
import com.makemission.makeflow.data.local.entity.TransactionEntity
import com.makemission.makeflow.data.local.userPrefDataStore
import com.makemission.makeflow.data.repository.FinanceRepositoryImpl
import com.makemission.makeflow.domain.AnomalyDetector
import com.makemission.makeflow.domain.MpesaBalanceCheck
import com.makemission.makeflow.domain.MpesaBalanceReconciler
import com.makemission.makeflow.domain.MpesaMessageParser
import com.makemission.makeflow.domain.MpesaZiidiTransferResolver
import com.makemission.makeflow.domain.TransactionCommitHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Locale

object MpesaBackgroundImporter {
    sealed interface Result {
        data class Saved(
            val balanceCheck: MpesaBalanceCheck?,
            val anomalyDetected: Boolean
        ) : Result
        data object ReviewRequired : Result
        data object AlreadyLogged : Result
    }

    private val importMutex = Mutex()

    suspend fun importIfSafe(
        context: Context,
        message: MpesaMessageParser.MpesaParsedMessage,
        logMpesaCosts: Boolean,
        operationId: String = message.operationId ?: AppLogger.newOperationId()
    ): Result = importMutex.withLock {
        AppLogger.event(
            tag = "MpesaBackgroundImporter",
            event = "import_started",
            operationId = operationId,
            details = mapOf(
                "source" to message.balanceSource,
                "message_type" to message.type,
                "amount" to message.amount,
                "closing_balance" to message.balance,
                "reference_hash" to AppLogger.hashIdentifier(message.code)
            )
        )
        val database = AppDatabase.getDatabase(context)
        val preferences = UserPreferencesDataStore(context.userPrefDataStore)
        val accounts = database.accountDao().getAllAccountsOnce()
        val mpesaAccounts = accounts.filter { it.name.contains("mpesa", ignoreCase = true) }
        if (mpesaAccounts.size != 1) {
            AppLogger.event("MpesaBackgroundImporter", "review_required", operationId, mapOf("reason" to "mpesa_account_count", "count" to mpesaAccounts.size), "WARN")
            return@withLock Result.ReviewRequired
        }
        val mpesaAccount = mpesaAccounts.single()
        val accountCurrency = mpesaAccount.currencyCode ?: preferences.selectedCurrency.first()
        if (!accountCurrency.equals("KES", ignoreCase = true)) {
            AppLogger.event("MpesaBackgroundImporter", "review_required", operationId, mapOf("reason" to "mpesa_account_currency", "currency" to accountCurrency), "WARN")
            return@withLock Result.ReviewRequired
        }

        val normalizedCounterparty = MpesaMessageParser.normalizeWhitespace(message.counterpartyName)
        val isZiidi = MpesaZiidiTransferResolver.isZiidiCounterparty(normalizedCounterparty)
        val ziidiAccount = if (isZiidi) {
            accounts.singleOrNull { MpesaZiidiTransferResolver.isZiidiAccountName(it.name) }
                ?: run {
                    AppLogger.event("MpesaBackgroundImporter", "review_required", operationId, mapOf("reason" to "ziidi_account_missing_or_ambiguous", "matches" to accounts.count { MpesaZiidiTransferResolver.isZiidiAccountName(it.name) }), "WARN")
                    return@withLock Result.ReviewRequired
                }
        } else {
            null
        }
        if (message.balanceSource == MpesaMessageParser.BalanceSource.ZIIDI && !isZiidi) {
            AppLogger.event("MpesaBackgroundImporter", "review_required", operationId, mapOf("reason" to "ziidi_balance_without_ziidi_counterparty"), "WARN")
            return@withLock Result.ReviewRequired
        }
        val transferAccounts = if (isZiidi) {
            MpesaZiidiTransferResolver.accounts(message.type, mpesaAccount.id, ziidiAccount!!.id)
                ?: run {
                    AppLogger.event("MpesaBackgroundImporter", "review_required", operationId, mapOf("reason" to "ziidi_direction_unresolved", "message_type" to message.type), "WARN")
                    return@withLock Result.ReviewRequired
                }
        } else {
            null
        }
        val timestamp = parseTimestamp(message.date, message.time)
            ?: message.timestampMillis
            ?: System.currentTimeMillis()
        val balanceAccount = when (message.balanceSource) {
            MpesaMessageParser.BalanceSource.MPESA -> mpesaAccount
            MpesaMessageParser.BalanceSource.ZIIDI -> ziidiAccount
        } ?: run {
            AppLogger.event("MpesaBackgroundImporter", "review_required", operationId, mapOf("reason" to "balance_account_missing"), "WARN")
            return@withLock Result.ReviewRequired
        }
        val appBalanceBeforeMessage = MpesaBalanceReconciler.estimateBalanceBefore(
            currentAccountBalance = balanceAccount.balance,
            accountId = balanceAccount.id,
            transactionTimestamp = timestamp,
            laterTransactions = database.transactionDao().getTransactionsSince(timestamp)
        )
        AppLogger.event(
            "MpesaBackgroundImporter",
            "direction_resolved",
            operationId,
            mapOf(
                "direction" to when {
                    !isZiidi -> "MPESA_ONLY"
                    transferAccounts?.fromAccountId == mpesaAccount.id -> "MPESA_TO_ZIIDI"
                    else -> "ZIIDI_TO_MPESA"
                },
                "from_account_hash" to AppLogger.hashIdentifier(transferAccounts?.fromAccountId ?: mpesaAccount.id),
                "to_account_hash" to AppLogger.hashIdentifier(transferAccounts?.toAccountId ?: mpesaAccount.id),
                "balance_account_hash" to AppLogger.hashIdentifier(balanceAccount.id),
                "account_balance_before" to balanceAccount.balance,
                "app_balance_before" to appBalanceBeforeMessage,
                "statement_balance" to message.balance
            )
        )

        val repository = FinanceRepositoryImpl(
            categoryDao = database.categoryDao(),
            transactionDao = database.transactionDao(),
            taskDao = database.taskDao(),
            accountDao = database.accountDao(),
            budgetDao = database.budgetDao(),
            recurringPatternDao = database.recurringPatternDao(),
            recurringTransactionDao = database.recurringTransactionDao()
        )
        if (repository.hasExternalReference(message.code)) {
            AppLogger.event("MpesaBackgroundImporter", "duplicate_skipped", operationId, mapOf("reference_hash" to AppLogger.hashIdentifier(message.code)))
            return@withLock Result.AlreadyLogged
        }

        val transactionType = if (isZiidi) {
            "TRANSFER"
        } else {
            when (message.type) {
                MpesaMessageParser.TransactionType.RECEIVED -> "INCOME"
                MpesaMessageParser.TransactionType.SENT,
                MpesaMessageParser.TransactionType.PAYBILL,
                MpesaMessageParser.TransactionType.WITHDRAW,
                MpesaMessageParser.TransactionType.AIRTIME -> "EXPENSE"
                MpesaMessageParser.TransactionType.UNKNOWN -> return@withLock Result.ReviewRequired
            }
        }
        val transaction = TransactionEntity(
            amount = message.amount,
            type = transactionType,
            accountId = if (isZiidi) "" else mpesaAccount.id,
            fromAccountId = transferAccounts?.fromAccountId,
            toAccountId = transferAccounts?.toAccountId,
            note = normalizedCounterparty.orEmpty(),
            timestamp = timestamp,
            externalReference = message.code
        )

        val hapticManager = HapticManager(context).apply {
            strength = HapticStrength.fromStorage(preferences.hapticLevel.first())
        }
        var anomalyDetected = false
        val commitHandler = TransactionCommitHandler(
            onCheckDuplicate = repository::checkDuplicate,
            onAnomalyCheck = { amount, categoryId ->
                if (categoryId == null) {
                    null
                } else {
                    AnomalyDetector.analyze(
                        amount,
                        database.transactionDao().getRecentAmountsForCategory(categoryId)
                    ).also { anomalyDetected = anomalyDetected || it.isAnomalous }
                }
            },
            hapticManager = hapticManager
        )
        var mainTransactionSaved = false
        commitHandler.prepareSave(
            entity = transaction,
            onSave = {
                repository.insertTransaction(it)
                mainTransactionSaved = true
            }
        )
        if (!mainTransactionSaved) {
            AppLogger.event("MpesaBackgroundImporter", "commit_not_saved", operationId, mapOf("reason" to "commit_handler_rejected"), "WARN")
            return@withLock Result.ReviewRequired
        }

        val cost = message.transactionCost?.takeIf { it.isFinite() && it > 0.0 }
        val feeReference = "${message.code}-fee"
        var feeSaved = false
        if (logMpesaCosts && cost != null && !repository.hasExternalReference(feeReference)) {
            try {
                val feeCategoryId = database.categoryDao().getAllCategoriesOnce().firstOrNull {
                    it.name == "Transaction Fees" && it.parentCategoryId != null
                }?.id
                val fee = TransactionEntity(
                    amount = cost,
                    type = "EXPENSE",
                    categoryId = feeCategoryId,
                    accountId = mpesaAccount.id,
                    note = "M-PESA transaction cost for ${message.code}",
                    timestamp = timestamp,
                    externalReference = feeReference
                )
                commitHandler.clearAlerts()
                commitHandler.prepareSave(
                    entity = fee,
                    onSave = {
                        repository.insertTransaction(it)
                        feeSaved = true
                    }
                )
            } catch (error: Exception) {
                AppLogger.error("MpesaBackgroundImporter", "Could not save the M-PESA transaction fee", error)
            }
        }

        val mpesaBalanceAfter = database.accountDao().getAccountById(mpesaAccount.id)?.balance
        val ziidiBalanceAfter = ziidiAccount?.let { database.accountDao().getAccountById(it.id)?.balance }
        Result.Saved(
            balanceCheck = appBalanceBeforeMessage?.let { balanceBefore ->
                MpesaBalanceReconciler.compare(
                    appBalanceBeforeTransaction = balanceBefore,
                    type = message.type,
                    amount = message.amount,
                    transactionCost = cost.takeIf { feeSaved },
                    statementBalance = message.balance,
                    balanceSource = message.balanceSource
                )
            },
            anomalyDetected = anomalyDetected
        ).also { result ->
            val check = result.balanceCheck
            AppLogger.event(
                "MpesaBackgroundImporter",
                "import_saved",
                operationId,
                mapOf(
                    "transaction_type" to transaction.type,
                    "from_account_hash" to AppLogger.hashIdentifier(transferAccounts?.fromAccountId),
                    "to_account_hash" to AppLogger.hashIdentifier(transferAccounts?.toAccountId),
                    "fee_saved" to feeSaved,
                    "balance_expected" to check?.expectedBalance,
                    "balance_statement" to check?.statementBalance,
                    "balance_matches" to check?.matches,
                    "mpesa_balance_after" to mpesaBalanceAfter,
                    "ziidi_balance_after" to ziidiBalanceAfter,
                    "anomaly_detected" to result.anomalyDetected
                )
            )
        }
    }

    private fun parseTimestamp(date: String, time: String): Long? = runCatching {
        val normalizedDate = date.replace(Regex("""/(\d{2})$""")) { "/20${it.groupValues[1]}" }
        SimpleDateFormat("d/M/yyyy h:mm a", Locale.getDefault()).apply {
            isLenient = false
        }.parse("$normalizedDate $time")?.time
    }.getOrNull()
}
