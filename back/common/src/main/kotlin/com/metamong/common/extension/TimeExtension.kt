package com.metamong.common.extension

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun LocalDate.endOfDay(): LocalDateTime = this.atTime(23, 59, 59)

fun LocalDateTime.withEndOfDay(): LocalDateTime =
    this
        .withHour(23)
        .withMinute(59)
        .withSecond(59)
        .withNano(0)

fun LocalDate.lastDayOfMonth(): LocalDate =
    this
        .withDayOfMonth(this.lengthOfMonth())

fun LocalDate.startDayOfMonth(): LocalDate =
    this
        .withDayOfMonth(1)

fun LocalDate.isEqualOrBefore(other: LocalDate): Boolean = this.isEqual(other) || this.isBefore(other)

fun ZonedDateTime.toKSTLocalDateTime(): LocalDateTime = this.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime()

fun YearMonth.startDateTime(): LocalDateTime = this.atDay(1).atStartOfDay()

fun YearMonth.endDateTime(): LocalDateTime = this.atEndOfMonth().endOfDay()

fun LocalDateTime.formatUntilHours(): String = this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"))

fun LocalDateTime.formatUntilMinutes(): String = this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

fun LocalDateTime.formatUntilSeconds(): String = this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
