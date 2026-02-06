package com.metamong.batch.jobs.publicdata.processor

import com.metamong.external.publicdata.PublicDataApiClient
import com.metamong.external.publicdata.PublicDataParser
import com.metamong.external.publicdata.dto.RegionCodeWithYearMonth
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

/**
 * Historical Job용 아파트 매매 실거래가 Processor
 * RegionCodeWithYearMonth에서 yearMonth를 직접 가져와서 처리
 */
@Component
class HistoricalApartmentTradeItemProcessor(
    private val publicDataApiClient: PublicDataApiClient,
    private val publicDataParser: PublicDataParser,
) : ItemProcessor<RegionCodeWithYearMonth, List<ApartmentTradeRawDocumentEntity>> {
    override fun process(item: RegionCodeWithYearMonth): List<ApartmentTradeRawDocumentEntity>? {
        val regionCode = item.regionCode
        val yearMonth = item.yearMonth

        logger.info { "아파트 매매 실거래가 처리 시작 - ${regionCode.sigunguNm} (${regionCode.sigunguCd}), $yearMonth" }

        val allDocuments = mutableListOf<ApartmentTradeRawDocumentEntity>()
        var pageNo = 1
        var hasMore = true

        while (hasMore) {
            val response =
                publicDataApiClient.fetchApartmentTrade(
                    lawdCd = regionCode.sigunguCd,
                    yearMonth = yearMonth,
                    pageNo = pageNo,
                )

            if (response.isNullOrBlank()) {
                if (pageNo == 1) {
                    logger.warn { "아파트 매매 실거래가 API 응답 없음 - ${regionCode.sigunguNm}, $yearMonth" }
                }
                break
            }

            val parseResult =
                publicDataParser.parseApartmentTrade(
                    response = response,
                    lawdCd = regionCode.sigunguCd,
                )

            allDocuments.addAll(parseResult.items)
            hasMore = parseResult.hasMore
            pageNo++

            if (hasMore) {
                logger.debug { "아파트 매매 실거래가 페이지네이션 - ${regionCode.sigunguNm}, $yearMonth: 페이지 $pageNo 조회 중" }
            }
        }

        logger.info { "아파트 매매 실거래가 처리 완료 - ${regionCode.sigunguNm}, $yearMonth: ${allDocuments.size}건" }
        return allDocuments.ifEmpty { null }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
