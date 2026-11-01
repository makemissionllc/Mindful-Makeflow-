package com.makemission.makeflow.domain

object MpesaMessageParser {

    fun isMpesaSender(sender: String?): Boolean {
        val normalized = sender
            ?.trim()
            ?.replace(Regex("[\\s\\p{Pd}]"), "")
            ?.uppercase(java.util.Locale.ROOT)
        return normalized == "MPESA"
    }

    enum class TransactionType {
        RECEIVED,
        SENT,
        PAYBILL,
        WITHDRAW,
        AIRTIME,
        UNKNOWN
    }

    enum class BalanceSource {
        MPESA,
        ZIIDI
    }

    data class MpesaParsedMessage(
        val code: String,
        val type: TransactionType,
        val amount: Double,
        val counterpartyName: String?,
        val counterpartyPhone: String?,
        val date: String,
        val time: String,
        val balance: Double?,
        val transactionCost: Double?,
        val isRatiba: Boolean = false,
        val rawCounterparty: String? = null,
        val isAiAssisted: Boolean = false,
        val balanceSource: BalanceSource = BalanceSource.MPESA,
        val timestampMillis: Long? = null,
        val operationId: String? = null
    )

    fun parse(text: String): MpesaParsedMessage? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        if (!trimmed.contains("confirmed", ignoreCase = true)) return null

        val code = extractCode(trimmed) ?: return null

        val isRatiba = Regex("confirmed\\s+M-?PESA\\s+ratiba", RegexOption.IGNORE_CASE)
            .containsMatchIn(trimmed)

        val dt = extractFlexibleDateTime(trimmed) ?: return null
        val (dateStr, timeStr) = dt

        val amounts = extractAmounts(trimmed)
        val amount = amounts.firstOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: return null

        val balance = extractFlexibleBalance(trimmed)
        val cost = extractFlexibleCost(trimmed)

        val typeAndCounterparty = detectTypeAndCounterparty(trimmed, dateStr)
        val type = typeAndCounterparty.first
        val counterpartyRaw = typeAndCounterparty.second

        val phone = extractPhone(counterpartyRaw)

        return MpesaParsedMessage(
            code = code,
            type = type,
            amount = amount,
            counterpartyName = normalizeWhitespace(counterpartyRaw)
                ?.trimEnd('.', ' ')
                ?.takeIf { it.isNotEmpty() },
            counterpartyPhone = phone,
            date = dateStr,
            time = timeStr,
            balance = balance,
            transactionCost = cost,
            isRatiba = isRatiba,
            rawCounterparty = counterpartyRaw?.trimEnd('.', ' ')?.takeIf { it.isNotEmpty() }
        )
    }

    fun parseAiClassification(response: String, source: String): MpesaParsedMessage? {
        if (!source.contains("confirmed", ignoreCase = true)) return null
        if (Regex("\\b(?:failed|unsuccessful|reversal|reversed)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(source)
        ) return null

        val start = response.indexOf('{')
        val end = response.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val fields = runCatching {
            com.google.gson.Gson().fromJson(response.substring(start, end + 1), Map::class.java)
        }.getOrNull() as? Map<*, *> ?: return null

        if ((fields["status"] as? String)?.equals("transaction", ignoreCase = true) != true) return null

        val code = extractCode(source) ?: return null
        if ((fields["code"] as? String)?.uppercase(java.util.Locale.ROOT) != code) return null

        val type = (fields["type"] as? String)
            ?.uppercase(java.util.Locale.ROOT)
            ?.let { name -> runCatching { TransactionType.valueOf(name) }.getOrNull() }
            ?.takeIf { it != TransactionType.UNKNOWN }
            ?: return null
        if (!hasTypeEvidence(type, source)) return null

        val amount = extractAmounts(source).firstOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val (date, time) = extractFlexibleDateTime(source) ?: return null
        val candidateName = (fields["counterparty"] as? String)
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it.length <= 100 }
            ?.takeIf { source.contains(it, ignoreCase = true) }

        return MpesaParsedMessage(
            code = code,
            type = type,
            amount = amount,
            counterpartyName = candidateName,
            counterpartyPhone = extractPhone(candidateName),
            date = date,
            time = time,
            balance = extractFlexibleBalance(source),
            transactionCost = extractFlexibleCost(source),
            isRatiba = Regex("confirmed\\s+M-?PESA\\s+ratiba", RegexOption.IGNORE_CASE)
                .containsMatchIn(source),
            rawCounterparty = candidateName,
            isAiAssisted = true
        )
    }

    fun normalizeWhitespace(text: String?): String? {
        if (text == null) return null
        return text.replace(Regex("\\s+"), " ").trim()
    }

    private fun extractCode(text: String): String? {
        val match = Regex("""^[A-Z0-9]+""").find(text) ?: return null
        val code = match.value
        return code.takeIf { it.length >= 4 }
    }

    internal fun extractAmounts(text: String): List<Double> {
        return Regex("""Ksh\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
            .findAll(text)
            .map { it.groupValues[1].replace(",", "").toDoubleOrNull() }
            .filterNotNull()
            .toList()
    }

    private fun extractPhone(text: String?): String? {
        if (text == null) return null
        val match = Regex("""(0\d{9,10}|(?:\d+)\*+(?:\d+))""").find(text)
        return match?.value
    }

    private fun extractFlexibleDateTime(text: String): Pair<String, String>? {
        val match = Regex(
            """on\s+(\d{1,2}/\d{1,2}/\d{2,4})\s+(?:at\s*)?(\d{1,2}:\d{2}(?:\s*[AP]M)?)""",
            RegexOption.IGNORE_CASE
        ).find(text) ?: return null
        val rawTime = match.groupValues[2].trim()
        val time = if (rawTime.endsWith("M", ignoreCase = true) || rawTime.endsWith("m", ignoreCase = true)) {
            rawTime
        } else {
            rawTime.uppercase().let { t ->
                when {
                    t.contains("AM") -> t
                    t.contains("PM") -> t
                    else -> "$t AM"
                }
            }
        }
        return match.groupValues[1] to time
    }

    private fun extractFlexibleBalance(text: String): Double? {
        val match = Regex(
            """balance\s+is\s+Ksh\s*([\d,]+(?:\.\d{1,2})?)""",
            RegexOption.IGNORE_CASE
        ).find(text) ?: return null
        return match.groupValues[1].replace(",", "").toDoubleOrNull()
    }

    private fun extractFlexibleCost(text: String): Double? {
        val match = Regex(
            """Transaction\s+cost,?\s*Ksh\s*([\d,]+(?:\.\d{1,2})?)""",
            RegexOption.IGNORE_CASE
        ).find(text) ?: return null
        return match.groupValues[1].replace(",", "").toDoubleOrNull()
    }

    private fun detectTypeAndCounterparty(
        text: String,
        dateStr: String
    ): Pair<TransactionType, String?> {
        val afterConfirmed = text.replaceFirst(Regex("""^[A-Z0-9]+\s+confirmed""", RegexOption.IGNORE_CASE), "")
            .trimStart('.', ' ', '\t')

        val onDatePattern = "on\\s+${Regex.escape(dateStr)}"
        val dateRegex = Regex(onDatePattern, RegexOption.IGNORE_CASE)

        val matchFrom = Regex("""received\s+Ksh\s*[\d,]+(?:\.\d{1,2})?\s+from\s+(.+?)\s+${onDatePattern}""",
            RegexOption.IGNORE_CASE).find(text)
        if (matchFrom != null) {
            val counterparty = matchFrom.groupValues[1].trim()
            return TransactionType.RECEIVED to counterparty
        }

        val matchSent = Regex("""sent\s+to\s+(.+?)\s+${onDatePattern}""",
            RegexOption.IGNORE_CASE).find(text)
        val matchPaid = Regex("""paid\s+to\s+(.+?)\s+${onDatePattern}""",
            RegexOption.IGNORE_CASE).find(text)

        if (matchPaid != null) {
            val counterparty = matchPaid.groupValues[1].trim()
            return TransactionType.PAYBILL to counterparty
        }

        if (matchSent != null) {
            val counterparty = matchSent.groupValues[1].trim()
            return TransactionType.SENT to counterparty
        }

        val matchWithdrawn = Regex("""withdrawn\s+from\s+(.+?)\s+${onDatePattern}""",
            RegexOption.IGNORE_CASE).find(text)
        if (matchWithdrawn != null) {
            val counterparty = matchWithdrawn.groupValues[1].trim()
            return TransactionType.WITHDRAW to counterparty
        }

        val matchAgentWithdrawal = Regex(
            """(?:AM|PM)?\s*withdraw\s+Ksh\s*[\d,]+(?:\.\d{1,2})?\s+from\s+(.+?)(?=\s+(?:New M-PESA balance|Transaction cost|Amount you can transact)|$)""",
            RegexOption.IGNORE_CASE
        ).find(text)
        if (matchAgentWithdrawal != null) {
            return TransactionType.WITHDRAW to matchAgentWithdrawal.groupValues[1].trim().trimEnd('.', ' ')
        }

        if (text.contains("airtime", ignoreCase = true)) {
            return TransactionType.AIRTIME to null
        }

        return TransactionType.UNKNOWN to null
    }

    private fun hasTypeEvidence(type: TransactionType, source: String): Boolean = when (type) {
        TransactionType.RECEIVED -> Regex("\\breceived\\s+Ksh", RegexOption.IGNORE_CASE)
            .containsMatchIn(source) || Regex(
            "Ksh\\s*[\\d,]+(?:\\.\\d{1,2})?\\s+credited\\s+to\\s+your\\s+M-PESA\\s+account",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(source)
        TransactionType.SENT -> Regex("\\bsent\\s+to\\b", RegexOption.IGNORE_CASE)
            .containsMatchIn(source)
        TransactionType.PAYBILL -> Regex("\\bpaid\\s+to\\b", RegexOption.IGNORE_CASE)
            .containsMatchIn(source) || Regex(
            "\\bsent\\s+to\\b.+?\\bfor\\s+account\\b",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(source)
        TransactionType.WITHDRAW -> Regex("\\bwithdraw(?:n)?\\b", RegexOption.IGNORE_CASE)
            .containsMatchIn(source)
        TransactionType.AIRTIME -> source.contains("airtime", ignoreCase = true) &&
            Regex("\\b(?:bought|purchase|purchased)\\b", RegexOption.IGNORE_CASE).containsMatchIn(source)
        TransactionType.UNKNOWN -> false
    }
}
