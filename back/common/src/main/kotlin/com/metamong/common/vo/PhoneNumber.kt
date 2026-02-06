package com.metamong.common.vo

@JvmInline
value class PhoneNumber(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "휴대폰 번호는 필수입니다" }
        require(PHONE_NUMBER_PATTERN.matches(value)) {
            "올바른 휴대폰 번호 형식이 아닙니다 (010-XXXX-XXXX)"
        }
    }

    companion object {
        private val PHONE_NUMBER_PATTERN = "^010-\\d{4}-\\d{4}$".toRegex()
    }
}
