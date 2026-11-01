package com.makemission.makeflow.domain

import com.makemission.makeflow.data.local.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MpesaBalanceReconcilerTest {
    @Test
    fun estimateBalanceBeforeReversesLaterAccountTransactions() {
        val estimate = MpesaBalanceReconciler.estimateBalanceBefore(
            currentAccountBalance = 1100.0,
            accountId = "mpesa",
            transactionTimestamp = 100_000L,
            laterTransactions = listOf(
                TransactionEntity(
                    amount = 200.0,
                    type = "INCOME",
                    accountId = "mpesa",
                    timestamp = 200_000L
                ),
                TransactionEntity(
                    amount = 100.0,
                    type = "EXPENSE",
                    accountId = "mpesa",
                    timestamp = 300_000L
                ),
                TransactionEntity(
                    amount = 500.0,
                    type = "EXPENSE",
                    accountId = "other",
                    timestamp = 400_000L
                )
            )
        )

        assertEquals(1000.0, estimate ?: 0.0, 0.0)
    }

    @Test
    fun estimateSkipsBalanceCheckWhenLaterActivitySharesSmsMinute() {
        val estimate = MpesaBalanceReconciler.estimateBalanceBefore(
            currentAccountBalance = 1100.0,
            accountId = "mpesa",
            transactionTimestamp = 100_000L,
            laterTransactions = listOf(
                TransactionEntity(
                    amount = 100.0,
                    type = "EXPENSE",
                    accountId = "mpesa",
                    timestamp = 120_000L
                )
            )
        )

        assertNull(estimate)
    }

    @Test
    fun outgoingPaymentIncludesTransactionFee() {
        val result = MpesaBalanceReconciler.compare(
            appBalanceBeforeTransaction = 1000.0,
            type = MpesaMessageParser.TransactionType.SENT,
            amount = 200.0,
            transactionCost = 10.0,
            statementBalance = 790.0
        )

        assertEquals(790.0, result?.expectedBalance ?: 0.0, 0.0)
        assertTrue(result?.matches == true)
    }

    @Test
    fun receivedPaymentIncreasesExpectedBalance() {
        val result = MpesaBalanceReconciler.compare(
            appBalanceBeforeTransaction = 1000.0,
            type = MpesaMessageParser.TransactionType.RECEIVED,
            amount = 200.0,
            transactionCost = null,
            statementBalance = 1200.0
        )

        assertTrue(result?.matches == true)
    }

    @Test
    fun ziidiInvestmentIncreasesZiidiBalance() {
        val result = MpesaBalanceReconciler.compare(
            appBalanceBeforeTransaction = 1000.0,
            type = MpesaMessageParser.TransactionType.SENT,
            amount = 200.0,
            transactionCost = null,
            statementBalance = 1200.0,
            balanceSource = MpesaMessageParser.BalanceSource.ZIIDI
        )

        assertEquals(1200.0, result?.expectedBalance ?: 0.0, 0.0)
        assertTrue(result?.matches == true)
    }

    @Test
    fun ziidiWithdrawalDecreasesZiidiBalance() {
        val result = MpesaBalanceReconciler.compare(
            appBalanceBeforeTransaction = 1000.0,
            type = MpesaMessageParser.TransactionType.RECEIVED,
            amount = 200.0,
            transactionCost = null,
            statementBalance = 800.0,
            balanceSource = MpesaMessageParser.BalanceSource.ZIIDI
        )

        assertEquals(800.0, result?.expectedBalance ?: 0.0, 0.0)
        assertTrue(result?.matches == true)
    }

    @Test
    fun mismatchReportsStatementDifference() {
        val result = MpesaBalanceReconciler.compare(
            appBalanceBeforeTransaction = 1000.0,
            type = MpesaMessageParser.TransactionType.PAYBILL,
            amount = 200.0,
            transactionCost = 10.0,
            statementBalance = 700.0
        )

        assertFalse(result?.matches == true)
        assertEquals(-90.0, result?.difference ?: 0.0, 0.0)
    }

    @Test
    fun missingOrUnknownStatementDataCannotBeCompared() {
        assertNull(
            MpesaBalanceReconciler.compare(
                appBalanceBeforeTransaction = 1000.0,
                type = MpesaMessageParser.TransactionType.UNKNOWN,
                amount = 200.0,
                transactionCost = null,
                statementBalance = 800.0
            )
        )
        assertNull(
            MpesaBalanceReconciler.compare(
                appBalanceBeforeTransaction = 1000.0,
                type = MpesaMessageParser.TransactionType.SENT,
                amount = 200.0,
                transactionCost = null,
                statementBalance = null
            )
        )
    }
}
