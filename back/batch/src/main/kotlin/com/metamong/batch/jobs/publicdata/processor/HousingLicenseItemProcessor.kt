package com.metamong.batch.jobs.publicdata.processor

import com.metamong.common.vo.LegalCode
import com.metamong.external.publicdata.PublicDataApiClient
import com.metamong.external.publicdata.PublicDataParser
import com.metamong.external.publicdata.dto.RegionCode
import com.metamong.infra.persistance.repository.publicdata.RegionLegalCodeRepository
import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class HousingLicenseItemProcessor(
    private val publicDataApiClient: PublicDataApiClient,
    private val publicDataParser: PublicDataParser,
    private val regionLegalCodeRepository: RegionLegalCodeRepository,
) : ItemProcessor<RegionCode, List<HousingLicenseRawDocumentEntity>> {
    override fun process(item: RegionCode): List<HousingLicenseRawDocumentEntity>? {
        val sidoCode = LegalCode.SidoCode(item.sidoCd)
        val sigunguCode = LegalCode.SigunguCode(item.sigunguCd.substring(2))

        val bjdongCodes =
            regionLegalCodeRepository
                .findLegalCodesBySidoAndSigungu(
                    sidoCode = sidoCode,
                    sigunguCode = sigunguCode,
                ).map { legalCode -> legalCode.code.substring(5) }

        if (bjdongCodes.isEmpty()) {
            logger.warn { "법정동코드 없음 - ${item.sigunguNm} (${item.sigunguCd})" }
            return null
        }

        logger.info { "주택인허가정보 처리 시작 - ${item.sigunguNm}: ${bjdongCodes.size}개 법정동" }

        val allDocuments = mutableListOf<HousingLicenseRawDocumentEntity>()

        for (bjdongCd in bjdongCodes) {
            val documents = fetchHousingLicenseByBjdong(item, bjdongCd)
            allDocuments.addAll(documents)
        }

        logger.info { "주택인허가정보 처리 완료 - ${item.sigunguNm}: ${allDocuments.size}건" }
        return allDocuments.ifEmpty { null }
    }

    private fun fetchHousingLicenseByBjdong(
        item: RegionCode,
        bjdongCd: String,
    ): List<HousingLicenseRawDocumentEntity> {
        val documents = mutableListOf<HousingLicenseRawDocumentEntity>()
        var pageNo = 1
        var hasMore = true

        while (hasMore) {
            val response =
                publicDataApiClient.fetchHousingLicense(
                    sigunguCd = item.sigunguCd,
                    bjdongCd = bjdongCd,
                    pageNo = pageNo,
                )

            if (response.isNullOrBlank()) {
                if (pageNo == 1) {
                    logger.debug { "주택인허가정보 API 응답 없음 - ${item.sigunguNm} ($bjdongCd)" }
                }
                break
            }

            val parseResult =
                publicDataParser.parseHousingLicense(
                    response = response,
                    sigunguCd = item.sigunguCd,
                    bjdongCd = bjdongCd,
                )

            documents.addAll(parseResult.items)

            logger.info {
                "주택인허가정보 조회 - ${item.sigunguNm} ($bjdongCd) " +
                    "페이지 $pageNo: ${parseResult.items.size}건 조회, " +
                    "totalCount=${parseResult.totalCount}, numOfRows=${parseResult.numOfRows}, " +
                    "hasMore=${parseResult.hasMore}"
            }

            hasMore = parseResult.hasMore
            pageNo++
        }

        return documents
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
