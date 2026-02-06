package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentSubscriptionRepository :
    JpaRepository<ApartmentSubscriptionEntity, Long>,
    ApartmentSubscriptionRepositoryCustom {
    fun existsByUserIdAndComplexIdAndUnitTypeId(
        userId: Long,
        complexId: Long,
        unitTypeId: Long?,
    ): Boolean

    fun deleteByUserIdAndComplexIdAndUnitTypeId(
        userId: Long,
        complexId: Long,
        unitTypeId: Long?,
    )
}
