package com.makemission.makeflow.domain

import kotlin.math.abs

data class MpesaBalanceCheck(
    val expectedBalance: Double,
    val statementBalance: Double
) {
    val difference: Double get() = statementBalance - expectedBalance
    val matches: Boolean get() = abs(difference) < 0.01
}

object MpesaBalanceReconciler {
    fun estimateBalanceBefore(
        currentAccountBalance: Double,
        accountId: String,
        transactionTimestamp: Long,
        laterTransactions: List<com.makemission.makeflow.data.local.entity.TransactionEntity>
    ): Double? {
        if (!currentAccountBalance.isFinite() || accountId.isBlank()) return null
        val closingMinute = transactionTimestamp + 60_000L
        if (laterTransactions.any { it.timestamp in transactionTimestamp until closingMinute }) return null
        val laterBalanceChange = laterTransactions.asSequence()
            .filter { it.timestamp > transactionTimestamp }
            .sumOf { transaction ->
                if (transaction.type == "TRANSFER") {
                    val amount = transaction.destinationAmount ?: transaction.amount
                    when {
                        transaction.fromAccountId == accountId -> -transaction.amount
                        transaction.toAccountId == accountId -> amount
                        else -> 0.0
                    }
                } else if (transaction.accountId == accountId) {
                    when (transaction.type) {
                        "INCOME" -> transaction.amount
                        "EXPENSE" -> -transaction.amount
                        else -> 0.0
                    }
                } else {
                    0.0
                }
            }
        return (currentAccountBalance - laterBalanceChange).takeIf { it.isFinite() }
    }

    fun compare(
        appBalanceBeforeTransaction: Double,
        type: MpesaMessageParser.TransactionType,
        amount: Double,
        transactionCost: Double?,
        statementBalance: Double?
    ): MpesaBalanceCheck? {
        if (!appBalanceBeforeTransaction.isFinite() || !amount.isFinite() || amount <= 0.0) return null
        val closingBalance = statementBalance?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
        val fee = transactionCost?.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
        val signedAmount = when (type) {
            MpesaMessageParser.TransactionType.RECEIVED -> amount
            MpesaMessageParser.TransactionType.SENT,
            MpesaMessageParser.TransactionType.PAYBILL,
            MpesaMessageParser.TransactionType.WITHDRAW,
            MpesaMessageParser.TransactionType.AIRTIME -> -amount
            MpesaMessageParser.TransactionType.UNKNOWN -> return null
        }
        return MpesaBalanceCheck(
            expectedBalance = appBalanceBeforeTransaction + signedAmount - fee,
            statementBalance = closingBalance
        )
    }
}
