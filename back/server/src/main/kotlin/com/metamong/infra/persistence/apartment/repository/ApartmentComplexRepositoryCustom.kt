package com.metamong.infra.persistence.apartment.repository

import com.metamong.infra.persistence.apartment.projection.ApartmentComplexListProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ApartmentComplexRepositoryCustom {
    fun findComplexesByConditions(
        sidoSigunguCode: Int,
        eupmyeondongCode: Int?,
        keyword: String?,
        pageable: Pageable,
    ): Page<ApartmentComplexListProjection>

    /**
     * 특정 시군구의 읍면동 코드 목록을 조회합니다.
     * @param sidoSigunguCode 시도시군구코드 5자리 (예: 11680)
     * @return 읍면동 코드 목록 (3자리, 중복 제거됨)
     */
    fun findDistinctEupmyeondongCodes(sidoSigunguCode: Int): List<Int>
}
