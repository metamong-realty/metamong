package com.metamong.infra.persistence.apartment.repository

import com.metamong.application.apartment.request.SortOrder
import com.metamong.infra.persistence.apartment.projection.ApartmentComplexListProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ApartmentComplexRepositoryCustom {
    fun findComplexesByConditions(
        sidoSigunguCode: Int,
        eupmyeondongCode: Int?,
        keyword: String?,
        sortOrder: SortOrder,
        pageable: Pageable,
    ): Page<ApartmentComplexListProjection>

    /**
     * 실제로 단지가 존재하는 시도 코드 목록을 조회합니다.
     * @return 시도 코드 목록 (2자리, 중복 제거됨)
     */
    fun findDistinctSidoCodes(): List<Int>

    /**
     * 실제로 단지가 존재하는 시도시군구 코드 목록을 조회합니다.
     * @return 시도시군구 코드 목록 (5자리, 중복 제거됨)
     */
    fun findDistinctSidoSigunguCodes(): List<Int>

    /**
     * 특정 시군구의 읍면동 코드 목록을 조회합니다.
     * @param sidoSigunguCode 시도시군구코드 5자리 (예: 11680)
     * @return 읍면동 코드 목록 (3자리, 중복 제거됨)
     */
    fun findDistinctEupmyeondongCodes(sidoSigunguCode: Int): List<Int>

    /**
     * 모든 시도시군구코드별 읍면동 코드 목록을 벌크로 조회합니다.
     * @return 시도시군구코드(5자리) → 읍면동 코드(3자리) 목록 매핑
     */
    fun findAllDistinctSidoSigunguAndEupmyeondongCodes(): Map<Int, List<Int>>
}
