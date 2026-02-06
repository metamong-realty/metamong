package com.metamong.enums.apartment

enum class RentType(
    val description: String,
) {
    JEONSE("전세"),
    MONTHLY("월세"),
    ;

    companion object {
        fun fromMonthlyRent(monthlyRent: Int): RentType = if (monthlyRent > 0) MONTHLY else JEONSE
    }
}
