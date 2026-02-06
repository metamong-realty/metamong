package com.metamong.batch.jobs.publicdata.processor

import com.metamong.external.publicdata.PublicDataApiClient
import com.metamong.external.publicdata.PublicDataParser
import com.metamong.external.publicdata.dto.RegionCode
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
class ApartmentRentItemProcessor(
    private val publicDataApiClient: PublicDataApiClient,
    private val publicDataParser: PublicDataParser,
) : ItemProcessor<RegionCode, List<ApartmentRentRawDocumentEntity>> {
    @Value(
        "#{jobParameters['yearMonth'] ?: T(java.time.LocalDate).now().format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMM'))}",
    )
    private lateinit var yearMonth: String

    override fun process(item: RegionCode): List<ApartmentRentRawDocumentEntity>? {
        logger.info { "아파트 전월세 실거래가 처리 시작 - ${item.sigunguNm} (${item.sigunguCd})" }

        val allDocuments = mutableListOf<ApartmentRentRawDocumentEntity>()
        var pageNo = 1
        var hasMore = true

        while (hasMore) {
            val response =
                publicDataApiClient.fetchApartmentRent(
                    lawdCd = item.sigunguCd,
                    yearMonth = yearMonth,
                    pageNo = pageNo,
                )

            if (response.isNullOrBlank()) {
                if (pageNo == 1) {
                    logger.warn { "아파트 전월세 실거래가 API 응답 없음 - ${item.sigunguNm}" }
                }
                break
            }

            val parseResult =
                publicDataParser.parseApartmentRent(
                    response = response,
                    lawdCd = item.sigunguCd,
                )

            allDocuments.addAll(parseResult.items)
            hasMore = parseResult.hasMore
            pageNo++

            if (hasMore) {
                logger.debug { "아파트 전월세 실거래가 페이지네이션 - ${item.sigunguNm}: 페이지 $pageNo 조회 중" }
            }
        }

        logger.info { "아파트 전월세 실거래가 처리 완료 - ${item.sigunguNm}: ${allDocuments.size}건" }
        return allDocuments.ifEmpty { null }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
