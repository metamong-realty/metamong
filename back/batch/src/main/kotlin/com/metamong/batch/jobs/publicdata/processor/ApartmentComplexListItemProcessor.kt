package com.metamong.batch.jobs.publicdata.processor

import com.metamong.external.publicdata.PublicDataApiClient
import com.metamong.external.publicdata.PublicDataParser
import com.metamong.external.publicdata.dto.RegionCode
import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class ApartmentComplexListItemProcessor(
    private val publicDataApiClient: PublicDataApiClient,
    private val publicDataParser: PublicDataParser,
) : ItemProcessor<RegionCode, List<ApartmentComplexListRawDocumentEntity>> {
    override fun process(item: RegionCode): List<ApartmentComplexListRawDocumentEntity>? {
        logger.info { "공동주택 단지 목록 처리 시작 - ${item.sigunguNm} (${item.sigunguCd})" }

        val allDocuments = mutableListOf<ApartmentComplexListRawDocumentEntity>()
        var pageNo = 1
        var hasMore = true

        while (hasMore) {
            val response =
                publicDataApiClient.fetchApartmentComplexList(
                    sigunguCd = item.sigunguCd,
                    pageNo = pageNo,
                )

            if (response.isNullOrBlank()) {
                if (pageNo == 1) {
                    logger.warn { "공동주택 단지 목록 API 응답 없음 - ${item.sigunguNm}" }
                }
                break
            }

            val parseResult = publicDataParser.parseApartmentComplexList(response = response)

            allDocuments.addAll(parseResult.items)
            hasMore = parseResult.hasMore
            pageNo++

            if (hasMore) {
                logger.debug { "공동주택 단지 목록 페이지네이션 - ${item.sigunguNm}: 페이지 $pageNo 조회 중" }
            }
        }

        logger.info { "공동주택 단지 목록 처리 완료 - ${item.sigunguNm}: ${allDocuments.size}건" }
        return allDocuments.ifEmpty { null }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
