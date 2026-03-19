package com.metamong.application.region.controller

import com.metamong.application.region.response.EupmyeondongResponse
import com.metamong.application.region.response.RegionAllResponse
import com.metamong.application.region.response.SidoResponse
import com.metamong.application.region.response.SigunguResponse
import com.metamong.application.region.service.RegionQueryService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [RegionQueryController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        SecurityFilterAutoConfiguration::class,
        OAuth2ClientAutoConfiguration::class,
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ["com\\.metamong\\.infra\\.security\\..*", "com\\.metamong\\.config\\.SecurityConfig"],
        ),
    ],
)
@DisplayName("RegionQueryController 테스트")
class RegionQueryControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var regionQueryService: RegionQueryService

    private fun stubAllRegions() {
        every { regionQueryService.getAllRegions() } returns
            RegionAllResponse(
                sido = listOf(SidoResponse("11", "서울특별시")),
                sigungu = mapOf("11" to listOf(SigunguResponse("680", "강남구"))),
                eupmyeondong = mapOf("11680" to listOf(EupmyeondongResponse("101", "역삼동"))),
            )
    }

    @Test
    @DisplayName("전체 지역 조회 API 호출 성공")
    fun `should return all regions successfully`() {
        stubAllRegions()

        mockMvc
            .perform(get("/v1/apartments/regions/all"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.sido").isArray)
            .andExpect(jsonPath("$.data.sigungu").isMap)
            .andExpect(jsonPath("$.data.eupmyeondong").isMap)
    }

    @Test
    @DisplayName("전체 지역 조회 응답 구조 검증")
    fun `should have correct response structure`() {
        stubAllRegions()

        mockMvc
            .perform(get("/v1/apartments/regions/all"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.sido").exists())
            .andExpect(jsonPath("$.data.sigungu").exists())
            .andExpect(jsonPath("$.data.eupmyeondong").exists())
            .andExpect(jsonPath("$.data.sido[0].code").exists())
            .andExpect(jsonPath("$.data.sido[0].name").exists())
    }
}
