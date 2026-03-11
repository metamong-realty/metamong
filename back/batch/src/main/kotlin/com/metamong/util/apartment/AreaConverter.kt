package com.metamong.util.apartment

import java.math.BigDecimal
import java.math.RoundingMode

object AreaConverter {
    private val PYEONG_DIVISOR = BigDecimal("3.3058")

    fun toPyeong(squareMeter: BigDecimal?): Int? {
        if (squareMeter == null || squareMeter <= BigDecimal.ZERO) return null

        return squareMeter
            .divide(PYEONG_DIVISOR, 0, RoundingMode.HALF_UP)
            .toInt()
    }

    fun toPyeong(squareMeterStr: String?): Int? {
        if (squareMeterStr.isNullOrBlank()) return null

        val squareMeter = squareMeterStr.trim().toBigDecimalOrNull() ?: return null
        return toPyeong(squareMeter)
    }

    fun parseExclusiveArea(exclusiveAreaStr: String?): BigDecimal? {
        if (exclusiveAreaStr.isNullOrBlank()) return null
        return exclusiveAreaStr.trim().toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_UP)
    }
}
