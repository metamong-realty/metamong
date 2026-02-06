package com.metamong.service.apartment.dto

import com.metamong.entity.apartment.ApartmentComplexEntity

data class ComplexWithApartmentSequence(
    val complex: ApartmentComplexEntity,
    val apartmentSequence: String,
)
