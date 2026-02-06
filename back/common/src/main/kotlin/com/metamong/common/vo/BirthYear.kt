package com.metamong.common.vo

import java.time.LocalDate
import java.time.Month
import java.time.Year

@JvmInline
value class BirthYear(
    val value: Int,
) {
    init {
        val currentYear = LocalDate.now().year
        require(value >= MIN_BIRTH_YEAR) { "생년은 ${MIN_BIRTH_YEAR}년 이후여야 합니다." }
        require(value <= currentYear) { "생년은 현재 년도($currentYear)보다 클 수 없습니다." }
    }

    companion object {
        private const val MIN_BIRTH_YEAR = 1900
    }
}

fun BirthYear.getStartOfYear(): LocalDate =
    Year
        .of(this.value)
        .atMonth(Month.JANUARY)
        .atDay(1)
