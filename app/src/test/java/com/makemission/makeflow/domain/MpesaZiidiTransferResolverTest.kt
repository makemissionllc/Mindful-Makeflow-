package com.makemission.makeflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MpesaZiidiTransferResolverTest {
    @Test
    fun sentMessageMapsMpesaAsSourceAndZiDiAsDestination() {
        val accounts = MpesaZiidiTransferResolver.accounts(
            type = MpesaMessageParser.TransactionType.SENT,
            mpesaAccountId = "mpesa",
            ziidiAccountId = "ziidi"
        )

        assertEquals("mpesa", accounts?.fromAccountId)
        assertEquals("ziidi", accounts?.toAccountId)
    }

    @Test
    fun receivedMessageMapsZiDiAsSourceAndMpesaAsDestination() {
        val accounts = MpesaZiidiTransferResolver.accounts(
            type = MpesaMessageParser.TransactionType.RECEIVED,
            mpesaAccountId = "mpesa",
            ziidiAccountId = "ziidi"
        )

        assertEquals("ziidi", accounts?.fromAccountId)
        assertEquals("mpesa", accounts?.toAccountId)
    }

    @Test
    fun unknownTypesAndBlankAccountsAreNotResolved() {
        assertNull(
            MpesaZiidiTransferResolver.accounts(
                type = MpesaMessageParser.TransactionType.UNKNOWN,
                mpesaAccountId = "mpesa",
                ziidiAccountId = "ziidi"
            )
        )
        assertNull(
            MpesaZiidiTransferResolver.accounts(
                type = MpesaMessageParser.TransactionType.SENT,
                mpesaAccountId = "",
                ziidiAccountId = "ziidi"
            )
        )
    }

    @Test
    fun accountMatchingSupportsZiDiAndZiidiLabels() {
        assertTrue(MpesaZiidiTransferResolver.isZiidiAccountName("ZiDi"))
        assertTrue(MpesaZiidiTransferResolver.isZiidiAccountName("Ziidi"))
        assertFalse(MpesaZiidiTransferResolver.isZiidiAccountName("ZiDi savings"))
    }
}
