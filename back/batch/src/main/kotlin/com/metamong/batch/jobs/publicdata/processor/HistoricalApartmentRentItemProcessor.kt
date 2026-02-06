package com.metamong.batch.jobs.publicdata.processor

import com.metamong.external.publicdata.PublicDataApiClient
import com.metamong.external.publicdata.PublicDataParser
import com.metamong.external.publicdata.dto.RegionCodeWithYearMonth
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

/**
 * Historical Job용 아파트 전월세 실거래가 Processor
 * RegionCodeWithYearMonth에서 yearMonth를 직접 가져와서 처리
 */
@Component
class HistoricalApartmentRentItemProcessor(
    private val publicDataApiClient: PublicDataApiClient,
    private val publicDataParser: PublicDataParser,
) : ItemProcessor<RegionCodeWithYearMonth, List<ApartmentRentRawDocumentEntity>> {
    override fun process(item: RegionCodeWithYearMonth): List<ApartmentRentRawDocumentEntity>? {
        val regionCode = item.regionCode
        val yearMonth = item.yearMonth

        logger.info { "아파트 전월세 실거래가 처리 시작 - ${regionCode.sigunguNm} (${regionCode.sigunguCd}), $yearMonth" }

        val allDocuments = mutableListOf<ApartmentRentRawDocumentEntity>()
        var pageNo = 1
        var hasMore = true

        while (hasMore) {
            val response =
                publicDataApiClient.fetchApartmentRent(
                    lawdCd = regionCode.sigunguCd,
                    yearMonth = yearMonth,
                    pageNo = pageNo,
                )

            if (response.isNullOrBlank()) {
                if (pageNo == 1) {
                    logger.warn { "아파트 전월세 실거래가 API 응답 없음 - ${regionCode.sigunguNm}, $yearMonth" }
                }
                break
            }

            val parseResult =
                publicDataParser.parseApartmentRent(
                    response = response,
                    lawdCd = regionCode.sigunguCd,
                )

            allDocuments.addAll(parseResult.items)
            hasMore = parseResult.hasMore
            pageNo++

            if (hasMore) {
                logger.debug { "아파트 전월세 실거래가 페이지네이션 - ${regionCode.sigunguNm}, $yearMonth: 페이지 $pageNo 조회 중" }
            }
        }

        logger.info { "아파트 전월세 실거래가 처리 완료 - ${regionCode.sigunguNm}, $yearMonth: ${allDocuments.size}건" }
        return allDocuments.ifEmpty { null }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
