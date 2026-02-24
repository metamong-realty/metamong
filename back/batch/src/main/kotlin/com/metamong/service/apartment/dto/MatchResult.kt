package com.metamong.service.apartment.dto

import com.metamong.domain.apartment.model.ApartmentCodeType
import com.metamong.domain.apartment.model.ApartmentComplexEntity

data class MatchResult(
    val complex: ApartmentComplexEntity,
    val codeMappingType: ApartmentCodeType?,
    val codeMappingValue: String?,
)
