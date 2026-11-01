package com.makemission.makeflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ZiidiMessageParserTest {
    @Test
    fun investedMessageMeansMoneyMovedIntoZiidiAndCapturesClosingBalance() {
        val parsed = ZiidiMessageParser.parse(
            "You have successfully invested Ksh. 123.45 of transaction code TST0000001. " +
                "Your ZIIDI balance is Ksh. 456.78",
            timestampMillis = 1234L
        )

        assertEquals("TST0000001", parsed?.code)
        assertEquals(MpesaMessageParser.TransactionType.SENT, parsed?.type)
        assertEquals(123.45, parsed?.amount ?: 0.0, 0.001)
        assertEquals(456.78, parsed?.balance ?: 0.0, 0.001)
        assertEquals(MpesaMessageParser.BalanceSource.ZIIDI, parsed?.balanceSource)
        assertEquals(1234L, parsed?.timestampMillis)
    }

    @Test
    fun withdrawnMessageMeansMoneyMovedOutOfZiidiAndCapturesClosingBalance() {
        val parsed = ZiidiMessageParser.parse(
            "You have successfully withdrawn Ksh. 23.45 of transaction code TST0000002. " +
                "Your ZIIDI balance is Ksh. 433.33."
        )

        assertEquals(MpesaMessageParser.TransactionType.RECEIVED, parsed?.type)
        assertEquals(433.33, parsed?.balance ?: 0.0, 0.001)
        assertEquals(MpesaMessageParser.BalanceSource.ZIIDI, parsed?.balanceSource)
    }

    @Test
    fun investmentCanBeParsedWhenMessageOmitsClosingBalance() {
        val parsed = ZiidiMessageParser.parse(
            "You have successfully invested Ksh. 9.25 of transaction code TST0000003."
        )

        assertEquals(MpesaMessageParser.TransactionType.SENT, parsed?.type)
        assertNull(parsed?.balance)
    }

    @Test
    fun balanceReminderIsNotTreatedAsATransaction() {
        assertNull(
            ZiidiMessageParser.parse(
                "Your ZIIDI investment balance is Ksh 20.38 on 2026-07-01 18:20:59."
            )
        )
    }

    @Test
    fun recognizesZiidiSenderAliases() {
        assertTrue(ZiidiMessageParser.isZiidiSender("ZIIDI"))
        assertTrue(ZiidiMessageParser.isZiidiSender("ZiDi"))
    }
}
