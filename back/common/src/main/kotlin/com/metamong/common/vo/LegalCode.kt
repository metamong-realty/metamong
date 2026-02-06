package com.metamong.common.vo

@JvmInline
value class LegalCode(
    val code: String,
) {
    fun getSidoCode(): SidoCode = SidoCode(code.substring(0, 2))

    fun getSigunguCode(): SigunguCode = SigunguCode(code.substring(2, 5))

    @JvmInline value class SidoCode(
        val code: String,
    ) {
        init {
            require(code.matches(Regex("\\d{2}"))) { "광역자치단체 식별코드는 2자리여야 합니다." }
        }
    }

    @JvmInline value class SigunguCode(
        val code: String,
    ) {
        init {
            require(code.matches(Regex("\\d{3}"))) { "기초자치단체 식별코드는 3자리여야 합니다." }
        }
    }

    @JvmInline value class EupmyeondongCode(
        val code: String,
    ) {
        init {
            require(code.matches(Regex("\\d{3}"))) { "읍·면·동 식별코드는 3자리여야 합니다." }
        }
    }

    @JvmInline value class RiCode(
        val code: String,
    ) {
        init {
            require(code.matches(Regex("\\d{2}"))) { "리·동·통 식별코드는 2자리여야 합니다." }
        }
    }

    init {
        require(code.length == 10) { "법정동 코드는 10자리여야 합니다." }
        require(code.all { it.isDigit() }) { "법정동 코드는 숫자만 포함해야 합니다." }
    }
}
