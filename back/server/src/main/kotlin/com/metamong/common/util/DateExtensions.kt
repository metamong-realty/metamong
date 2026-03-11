package com.metamong.common.util

import java.time.LocalDate

/**
 * 년월을 월 단위 정수로 변환
 * 예: 2024년 3월 = 2024 * 12 + 3 = 24291
 */
fun LocalDate.toMonthValue(): Int = this.year * 12 + this.monthValue

/**
 * 년월을 월 단위 정수로 변환 (contractYear, contractMonth용)
 * Short 타입도 지원
 */
fun toMonthValue(year: Number, month: Number): Int = 
    year.toInt() * 12 + month.toInt()
