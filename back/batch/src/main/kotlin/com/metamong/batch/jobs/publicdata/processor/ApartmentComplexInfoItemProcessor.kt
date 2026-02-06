package com.metamong.batch.jobs.publicdata.processor

import com.metamong.external.publicdata.PublicDataApiClient
import com.metamong.external.publicdata.PublicDataParser
import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class ApartmentComplexInfoItemProcessor(
    private val publicDataApiClient: PublicDataApiClient,
    private val publicDataParser: PublicDataParser,
) : ItemProcessor<String, ApartmentComplexInfoRawDocumentEntity?> {
    override fun process(kaptCode: String): ApartmentComplexInfoRawDocumentEntity? {
        logger.debug { "공동주택 기본 정보 처리 시작 - kaptCode: $kaptCode" }

        val response = publicDataApiClient.fetchApartmentComplexInfo(kaptCode)

        if (response.isNullOrBlank()) {
            logger.warn { "공동주택 기본 정보 API 응답 없음 - kaptCode: $kaptCode" }
            return null
        }

        val document =
            publicDataParser.parseApartmentComplexInfo(
                response = response,
                kaptCode = kaptCode,
            )

        if (document != null) {
            logger.debug { "공동주택 기본 정보 처리 완료 - ${document.kaptName}" }
        }

        return document
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
