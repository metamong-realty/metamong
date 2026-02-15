package com.metamong.batch.jobs.publicdata.sync

import org.springframework.data.mongodb.core.query.Criteria
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DealYearMonthRange private constructor(
    private val startYearMonth: YearMonth,
    private val endYearMonth: YearMonth,
) {
    private val criteria: Criteria = createCriteria()

    fun buildCriteria(): Criteria = criteria

    override fun toString(): String {
        val fmt = DateTimeFormatter.ofPattern("yyyyMM")
        return "${startYearMonth.format(fmt)}~${endYearMonth.format(fmt)}"
    }

    private fun createCriteria(): Criteria {
        val yearMonthsByYear = generateYearMonths().groupBy({ it.first }, { it.second })

        if (yearMonthsByYear.size == 1) {
            val (year, months) = yearMonthsByYear.entries.first()
            return Criteria
                .where("dealYear")
                .`is`(year)
                .and("dealMonth")
                .`in`(months)
        }

        val orCriteria =
            yearMonthsByYear.map { (year, months) ->
                Criteria().andOperator(
                    Criteria.where("dealYear").`is`(year),
                    Criteria.where("dealMonth").`in`(months),
                )
            }
        return Criteria().orOperator(*orCriteria.toTypedArray())
    }

    private fun generateYearMonths(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        var current = startYearMonth

        while (!current.isAfter(endYearMonth)) {
            val year = current.year.toString()
            val month = current.monthValue
            result.add(year to month.toString())
            if (month in 1..9) {
                result.add(year to "0$month")
            }
            current = current.plusMonths(1)
        }

        return result
    }

    companion object {
        private val YEAR_MONTH_PATTERN = Regex("^\\d{6}$")
        private val FORMATTER = DateTimeFormatter.ofPattern("yyyyMM")

        fun of(
            startYearMonth: String?,
            endYearMonth: String?,
        ): DealYearMonthRange? {
            if (startYearMonth == null && endYearMonth == null) return null
            requireNotNull(startYearMonth) { "startYearMonth와 endYearMonth는 함께 제공되어야 합니다" }
            requireNotNull(endYearMonth) { "startYearMonth와 endYearMonth는 함께 제공되어야 합니다" }
            require(YEAR_MONTH_PATTERN.matches(startYearMonth)) { "startYearMonth는 yyyyMM 형식이어야 합니다: $startYearMonth" }
            require(YEAR_MONTH_PATTERN.matches(endYearMonth)) { "endYearMonth는 yyyyMM 형식이어야 합니다: $endYearMonth" }

            val start = YearMonth.parse(startYearMonth, FORMATTER)
            val end = YearMonth.parse(endYearMonth, FORMATTER)
            require(!start.isAfter(end)) { "startYearMonth($startYearMonth)는 endYearMonth($endYearMonth)보다 클 수 없습니다" }

            return DealYearMonthRange(start, end)
        }
    }
}
