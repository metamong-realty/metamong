package com.metamong.service.apartment.dto

import com.metamong.domain.apartment.model.ApartmentComplexEntity

data class ComplexWithApartmentSequence(
    val complex: ApartmentComplexEntity,
    val apartmentSequence: String,
)
