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
        logMpesaCosts: Boolean
    ): Result = importMutex.withLock {
        val database = AppDatabase.getDatabase(context)
        val preferences = UserPreferencesDataStore(context.userPrefDataStore)
        val accounts = database.accountDao().getAllAccountsOnce()
        val mpesaAccounts = accounts.filter { it.name.contains("mpesa", ignoreCase = true) }
        if (mpesaAccounts.size != 1) return@withLock Result.ReviewRequired
        val mpesaAccount = mpesaAccounts.single()
        val accountCurrency = mpesaAccount.currencyCode ?: preferences.selectedCurrency.first()
        if (!accountCurrency.equals("KES", ignoreCase = true)) {
            return@withLock Result.ReviewRequired
        }

        val normalizedCounterparty = MpesaMessageParser.normalizeWhitespace(message.counterpartyName)
        val isZiidi = normalizedCounterparty.equals("ZIIDI", ignoreCase = true)
        val ziidiAccount = if (isZiidi) {
            accounts.singleOrNull { it.name.equals("Ziidi", ignoreCase = true) }
                ?: return@withLock Result.ReviewRequired
        } else {
            null
        }
        val timestamp = parseTimestamp(message.date, message.time)
            ?: return@withLock Result.ReviewRequired
        val appBalanceBeforeMessage = MpesaBalanceReconciler.estimateBalanceBefore(
            currentAccountBalance = mpesaAccount.balance,
            accountId = mpesaAccount.id,
            transactionTimestamp = timestamp,
            laterTransactions = database.transactionDao().getTransactionsSince(timestamp)
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
        if (repository.hasExternalReference(message.code)) return@withLock Result.AlreadyLogged

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
            fromAccountId = if (isZiidi) mpesaAccount.id else null,
            toAccountId = ziidiAccount?.id,
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
        if (!mainTransactionSaved) return@withLock Result.ReviewRequired

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

        Result.Saved(
            balanceCheck = appBalanceBeforeMessage?.let { balanceBefore ->
                MpesaBalanceReconciler.compare(
                    appBalanceBeforeTransaction = balanceBefore,
                    type = message.type,
                    amount = message.amount,
                    transactionCost = cost.takeIf { feeSaved },
                    statementBalance = message.balance
                )
            },
            anomalyDetected = anomalyDetected
        )
    }

    private fun parseTimestamp(date: String, time: String): Long? = runCatching {
        val normalizedDate = date.replace(Regex("""/(\d{2})$""")) { "/20${it.groupValues[1]}" }
        SimpleDateFormat("d/M/yyyy h:mm a", Locale.getDefault()).apply {
            isLenient = false
        }.parse("$normalizedDate $time")?.time
    }.getOrNull()
}
