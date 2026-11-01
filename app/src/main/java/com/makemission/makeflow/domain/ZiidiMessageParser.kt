package com.makemission.makeflow.domain

object ZiidiMessageParser {
    private val investmentPattern = Regex(
        """successfully\s+invested\s+Ksh\.?\s*([\d,]+(?:\.\d{1,2})?)\s+of\s+transaction\s+code\s+([A-Z0-9]+)""",
        RegexOption.IGNORE_CASE
    )
    private val withdrawalPattern = Regex(
        """successfully\s+withdrawn\s+Ksh\.?\s*([\d,]+(?:\.\d{1,2})?)\s+of\s+transaction\s+code\s+([A-Z0-9]+)""",
        RegexOption.IGNORE_CASE
    )
    private val balancePattern = Regex(
        """ZIIDI(?:\s+investment)?\s+balance\s+is\s+Ksh\.?\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    fun isZiidiSender(sender: String?): Boolean {
        val normalized = sender
            ?.trim()
            ?.replace(Regex("[\\s\\p{Pd}]"), "")
            ?.uppercase(java.util.Locale.ROOT)
        return normalized == "ZIIDI" || normalized == "ZIDI"
    }

    fun parse(text: String, timestampMillis: Long? = null): MpesaMessageParser.MpesaParsedMessage? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        val investment = investmentPattern.find(trimmed)
        val withdrawal = withdrawalPattern.find(trimmed)
        val match = investment ?: withdrawal ?: return null
        val amount = match.groupValues[1].replace(",", "").toDoubleOrNull()
            ?.takeIf { it.isFinite() && it > 0.0 }
            ?: return null
        val code = match.groupValues[2].uppercase(java.util.Locale.ROOT)
            .takeIf { it.length in 4..20 }
            ?: return null
        val balance = balancePattern.find(trimmed)?.groupValues?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?.takeIf { it.isFinite() && it >= 0.0 }

        return MpesaMessageParser.MpesaParsedMessage(
            code = code,
            type = if (investment != null) {
                MpesaMessageParser.TransactionType.SENT
            } else {
                MpesaMessageParser.TransactionType.RECEIVED
            },
            amount = amount,
            counterpartyName = "ZIIDI",
            counterpartyPhone = null,
            date = "",
            time = "",
            balance = balance,
            transactionCost = null,
            rawCounterparty = "ZIIDI",
            balanceSource = MpesaMessageParser.BalanceSource.ZIIDI,
            timestampMillis = timestampMillis
        )
    }
}
