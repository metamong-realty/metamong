package com.metamong.batch.jobs.publicdata.reader

import com.metamong.external.publicdata.dto.RegionCode
import com.metamong.external.publicdata.dto.RegionCodeWithYearMonth
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Historical Job용 Reader
 * startYearMonth ~ endYearMonth 범위의 모든 (RegionCode, YearMonth) 조합을 생성
 *
 * 예: startYearMonth=202301, endYearMonth=202303, 지역 2개
 * → (지역1, 202301), (지역1, 202302), (지역1, 202303), (지역2, 202301), ...
 */
@Component
@StepScope
class RegionCodeYearMonthItemReader(
    @Value("#{jobParameters['startYearMonth']}")
    private val startYearMonth: String,
    @Value("#{jobParameters['endYearMonth']}")
    private val endYearMonth: String,
) : ItemReader<RegionCodeWithYearMonth> {
    private var initialized = false
    private lateinit var items: Iterator<RegionCodeWithYearMonth>

    override fun read(): RegionCodeWithYearMonth? {
        if (!initialized) {
            initialize()
        }
        return if (items.hasNext()) items.next() else null
    }

    private fun initialize() {
        val yearMonths = generateYearMonthRange(startYearMonth, endYearMonth)
        val regionCodes = RegionCode.getTargetRegions()

        logger.info {
            "Historical Reader 초기화 - 기간: $startYearMonth ~ $endYearMonth (${yearMonths.size}개월), " +
                "지역: ${regionCodes.size}개, 총 조합: ${yearMonths.size * regionCodes.size}건"
        }

        // 지역별로 모든 월을 처리 (지역1의 모든 월 → 지역2의 모든 월 → ...)
        items =
            regionCodes
                .flatMap { regionCode ->
                    yearMonths.map { yearMonth ->
                        RegionCodeWithYearMonth(regionCode, yearMonth)
                    }
                }.iterator()

        initialized = true
    }

    private fun generateYearMonthRange(
        start: String,
        end: String,
    ): List<String> {
        val formatter = DateTimeFormatter.ofPattern("yyyyMM")
        val startYm = YearMonth.parse(start, formatter)
        val endYm = YearMonth.parse(end, formatter)

        val result = mutableListOf<String>()
        var current = startYm
        while (!current.isAfter(endYm)) {
            result.add(current.format(formatter))
            current = current.plusMonths(1)
        }
        return result
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
