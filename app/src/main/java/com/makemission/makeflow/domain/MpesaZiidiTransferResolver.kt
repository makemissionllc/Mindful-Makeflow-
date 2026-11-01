package com.makemission.makeflow.domain

object MpesaZiidiTransferResolver {
    enum class Direction {
        TO_ZIIDI,
        FROM_ZIIDI
    }

    data class Accounts(
        val fromAccountId: String,
        val toAccountId: String
    )

    fun isZiidiCounterparty(name: String?): Boolean = isZiidiAccountName(name)

    fun isZiidiAccountName(name: String?): Boolean {
        val normalized = name
            ?.trim()
            ?.uppercase(java.util.Locale.ROOT)
            ?.filter { it.isLetterOrDigit() }
            ?: return false
        return normalized == "ZIDI" || normalized == "ZIIDI"
    }

    fun direction(type: MpesaMessageParser.TransactionType): Direction? = when (type) {
        MpesaMessageParser.TransactionType.SENT -> Direction.TO_ZIIDI
        MpesaMessageParser.TransactionType.RECEIVED -> Direction.FROM_ZIIDI
        else -> null
    }

    fun accounts(
        type: MpesaMessageParser.TransactionType,
        mpesaAccountId: String,
        ziidiAccountId: String
    ): Accounts? {
        if (mpesaAccountId.isBlank() || ziidiAccountId.isBlank()) return null
        return when (direction(type)) {
            Direction.TO_ZIIDI -> Accounts(fromAccountId = mpesaAccountId, toAccountId = ziidiAccountId)
            Direction.FROM_ZIIDI -> Accounts(fromAccountId = ziidiAccountId, toAccountId = mpesaAccountId)
            null -> null
        }
    }
}
