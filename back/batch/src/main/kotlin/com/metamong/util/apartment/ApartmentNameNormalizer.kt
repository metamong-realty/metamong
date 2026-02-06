package com.metamong.util.apartment

object ApartmentNameNormalizer {
    private val BRAND_MAPPINGS =
        mapOf(
            "xi" to "자이",
            "XI" to "자이",
            "Xi" to "자이",
            "lotte castle" to "롯데캐슬",
            "LOTTE CASTLE" to "롯데캐슬",
            "hillstate" to "힐스테이트",
            "HILLSTATE" to "힐스테이트",
            "prugio" to "푸르지오",
            "PRUGIO" to "푸르지오",
            "raemian" to "래미안",
            "RAEMIAN" to "래미안",
            "이편한세상" to "이편한세상",
        )

    private val REMOVE_PATTERNS =
        listOf(
            Regex("""\([^)]*\)"""),
            Regex("""\[[^\]]*\]"""),
            Regex("""아파트$"""),
            Regex("""APT$""", RegexOption.IGNORE_CASE),
        )

    private val WHITESPACE_PATTERN = Regex("""\s+""")

    fun normalize(name: String?): String? {
        if (name.isNullOrBlank()) return null

        var normalized = name.trim()

        REMOVE_PATTERNS.forEach { pattern ->
            normalized = pattern.replace(normalized, "")
        }

        BRAND_MAPPINGS.forEach { (english, korean) ->
            normalized = normalized.replace(english, korean, ignoreCase = true)
        }

        normalized = WHITESPACE_PATTERN.replace(normalized, "")

        return normalized.takeIf { it.isNotBlank() }
    }
}
