package com.metamong.application.region.controller

import com.metamong.application.region.service.RegionQueryService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RegionQueryController 테스트")
class RegionQueryControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var regionQueryService: RegionQueryService

    @Test
    @DisplayName("전체 지역 조회 API 호출 성공")
    fun `should return all regions successfully`() {
        // when & then
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
        // when & then
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
