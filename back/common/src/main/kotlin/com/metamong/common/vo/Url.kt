package com.metamong.common.vo

import java.net.URLEncoder

@JvmInline
value class Url(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "URL은 비어 있을 수 없습니다." }
        require(value.startsWith("http://") || value.startsWith("https://")) { "유효한 URL 형식이 아닙니다." }
        require(!containsMalicious(value)) { "URL에 허용하지 않는 문자가 포함되어 있습니다." }
    }

    fun encodeUrlParameters(): Url {
        val baseUrl = this.value.substringBefore("?")
        val params = this.value.substringAfter("?", "")

        if (params.isEmpty()) return Url(baseUrl)

        val encodedParams =
            params.split("&").joinToString("&") { param ->
                val (key, value) = param.split("=", limit = 2)
                "$key=${URLEncoder.encode(value, "UTF-8")}"
            }
        return Url("$baseUrl?$encodedParams")
    }

    fun substringAfterLast(delimiter: String): String = value.substringAfterLast(delimiter)

    fun containsMalicious(url: String): Boolean {
        val lower = url.lowercase()
        val any =
            listOf(
                "<",
                ">",
                "\"",
                "'",
                "javascript:",
                "data:",
                "vbscript:",
            ).any { it in lower }
        return any
    }
}
