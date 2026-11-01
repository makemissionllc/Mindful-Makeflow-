package com.makemission.makeflow.core.notifications

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MpesaAutoImportNotificationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun savedNotificationCarriesOnlyAValidTransactionId() {
        val transactionId = "00000000-0000-4000-8000-000000000001"
        val intent = MpesaAutoImportNotification.createCategorizeSavedIntent(context, transactionId)

        assertEquals(transactionId, MpesaAutoImportNotification.readSavedTransactionId(intent))
        assertNull(
            MpesaAutoImportNotification.readSavedTransactionId(
                MpesaAutoImportNotification.createCategorizeSavedIntent(context, "invalid-id")
            )
        )
        assertNull(
            MpesaAutoImportNotification.readSavedTransactionId(
                Intent().setAction(MpesaAutoImportNotification.ACTION_REVIEW)
            )
        )
    }
}
