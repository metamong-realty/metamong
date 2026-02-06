package com.metamong.domain.apartment.model

enum class PlatType(
    val code: String,
) {
    LAND("0"),
    MOUNTAIN("1"),
    ;

    companion object {
        fun fromCode(code: String?): PlatType? = entries.find { it.code == code }
    }
}
