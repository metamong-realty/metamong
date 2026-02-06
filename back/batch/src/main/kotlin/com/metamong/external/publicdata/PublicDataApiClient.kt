package com.metamong.external.publicdata

import com.metamong.external.publicdata.dto.PublicDataApiType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry
import java.time.Duration

@Component
class PublicDataApiClient(
    @Qualifier("publicDataClient") private val webClient: WebClient,
) {
    @Value("\${public-data.service-key:}")
    private lateinit var serviceKey: String

    /**
     * 아파트 매매 실거래가 조회 (JSON)
     * @param lawdCd 지역코드 (5자리)
     * @param yearMonth 계약년월 (YYYYMM)
     * @param pageNo 페이지 번호 (기본값: 1)
     */
    fun fetchApartmentTrade(
        lawdCd: String,
        yearMonth: String,
        pageNo: Int = 1,
    ): String? =
        fetchWithRetry(
            apiType = PublicDataApiType.APARTMENT_TRADE,
            params =
                mapOf(
                    "LAWD_CD" to lawdCd,
                    "DEAL_YMD" to yearMonth,
                    "numOfRows" to PAGE_SIZE.toString(),
                    "pageNo" to pageNo.toString(),
                ),
        )

    /**
     * 아파트 전월세 실거래가 조회 (JSON)
     * @param lawdCd 지역코드 (5자리)
     * @param yearMonth 계약년월 (YYYYMM)
     * @param pageNo 페이지 번호 (기본값: 1)
     */
    fun fetchApartmentRent(
        lawdCd: String,
        yearMonth: String,
        pageNo: Int = 1,
    ): String? =
        fetchWithRetry(
            apiType = PublicDataApiType.APARTMENT_RENT,
            params =
                mapOf(
                    "LAWD_CD" to lawdCd,
                    "DEAL_YMD" to yearMonth,
                    "numOfRows" to PAGE_SIZE.toString(),
                    "pageNo" to pageNo.toString(),
                ),
        )

    /**
     * 주택인허가정보 조회 (JSON)
     * @param sigunguCd 시군구코드 (5자리)
     * @param bjdongCd 법정동코드 (5자리)
     * @param pageNo 페이지 번호 (기본값: 1)
     */
    fun fetchHousingLicense(
        sigunguCd: String,
        bjdongCd: String,
        pageNo: Int = 1,
    ): String? =
        fetchWithRetry(
            apiType = PublicDataApiType.HOUSING_LICENSE,
            params =
                mapOf(
                    "sigunguCd" to sigunguCd,
                    "bjdongCd" to bjdongCd,
                    "numOfRows" to PAGE_SIZE.toString(),
                    "pageNo" to pageNo.toString(),
                ),
        )

    /**
     * 공동주택 단지 목록 조회 (JSON)
     * @param sidoCd 시도코드 (2자리)
     * @param sigunguCd 시군구코드 (5자리)
     * @param pageNo 페이지 번호 (기본값: 1)
     */
    fun fetchApartmentComplexList(
        sigunguCd: String,
        pageNo: Int = 1,
    ): String? =
        fetchWithRetry(
            apiType = PublicDataApiType.APARTMENT_COMPLEX_LIST,
            params =
                mapOf(
                    "sigunguCode" to sigunguCd,
                    "numOfRows" to PAGE_SIZE.toString(),
                    "pageNo" to pageNo.toString(),
                ),
        )

    /**
     * 공동주택 기본 정보 조회 (JSON)
     * @param kaptCode 단지코드
     */
    fun fetchApartmentComplexInfo(kaptCode: String): String? =
        fetchWithRetry(
            apiType = PublicDataApiType.APARTMENT_COMPLEX_INFO,
            params = mapOf("kaptCode" to kaptCode),
        )

    private fun fetchWithRetry(
        apiType: PublicDataApiType,
        params: Map<String, String>,
    ): String? {
        logger.info { "공공데이터 API 호출: ${apiType.description} - params: $params" }

        return runCatching {
            webClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(apiType.path)
                        .queryParam("serviceKey", serviceKey)
                        .also { builder ->
                            builder.queryParam("type", "json")
                            params.forEach { (key, value) ->
                                builder.queryParam(key, value)
                            }
                        }.build()
                }.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java)
                .retryWhen(
                    Retry
                        .backoff(MAX_RETRY_COUNT, Duration.ofSeconds(RETRY_DELAY_SECONDS))
                        .doBeforeRetry { signal ->
                            logger.warn { "API 호출 재시도 (${signal.totalRetries() + 1}/$MAX_RETRY_COUNT): ${apiType.description}" }
                        },
                ).doOnError { error ->
                    logger.error(error) { "API 호출 실패: ${apiType.description} - ${error.message}" }
                }.block()
        }.onFailure { e ->
            logger.error(e) { "API 호출 최종 실패: ${apiType.description}" }
        }.getOrNull()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_RETRY_COUNT = 3L
        private const val RETRY_DELAY_SECONDS = 2L
        const val PAGE_SIZE = 10000
    }
}
