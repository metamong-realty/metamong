package com.metamong.common.extension

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.parseToKst(): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss") // ISO 형식
    val utcDateTime = LocalDateTime.parse(this, formatter)
    return utcDateTime
        .atZone(ZoneId.of("UTC")) // UTC로 인식
        .withZoneSameInstant(ZoneId.of("Asia/Seoul")) // KST로 변환
        .toLocalDateTime()
}

fun String.trimToOneLine() = trimIndent().replace("\n+".toRegex(), replacement = "")

fun String?.orElse(default: String): String = this ?: default

fun String.decodeUnicode(): String {
    val result = StringBuilder()
    val length = this.length
    var i = 0

    while (i < length) {
        val currentChar = this[i]
        if (currentChar == '\\' && i + 1 < length && this[i + 1] == 'u') {
            val unicodeHex = this.substring(i + 2, i + 6)
            val unicodeValue = unicodeHex.toInt(16)
            result.append(unicodeValue.toChar())
            i += 5
        } else {
            result.append(currentChar)
        }
        i++
    }

    return result.toString()
}

fun String?.addOrUpdateUrlUtmSource(utmSource: String): String? {
    if (this == null) return null

    val utmParamPattern = "utm_source=[^&]*".toRegex()

    return when {
        // URL에 ?가 없는 경우 - 쿼리 파라미터 추가
        !this.contains("?") -> "$this?utm_source=$utmSource"

        // 이미 utm_source가 있는 경우 - 값 교체
        utmParamPattern.containsMatchIn(this) ->
            this.replace(utmParamPattern, "utm_source=$utmSource")

        // ?는 있지만 utm_source가 없는 경우 - & 로 추가
        else -> "$this&utm_source=$utmSource"
    }
}
