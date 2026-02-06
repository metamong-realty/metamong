package com.metamong.util.apartment

object PriceParser {
    fun parsePrice(price: String?): Int? {
        if (price.isNullOrBlank()) return null

        val cleaned = price.replace(",", "").replace(" ", "")
        return cleaned.toIntOrNull()
    }

    fun parsePriceOrZero(price: String?): Int = parsePrice(price) ?: 0
}
