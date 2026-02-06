package com.metamong.infrastructure.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentSubscriptionEntity

interface ApartmentSubscriptionRepositoryCustom {
    fun findByUserId(userId: Long): List<ApartmentSubscriptionEntity>
}