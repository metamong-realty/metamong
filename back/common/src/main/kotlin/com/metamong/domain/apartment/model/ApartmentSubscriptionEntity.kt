package com.metamong.domain.apartment.model

import com.metamong.domain.base.BaseCreatedEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "apartment_subscriptions")
class ApartmentSubscriptionEntity(
    val userId: Long,
    val complexId: Long,
    val unitTypeId: Long? = null,
) : BaseCreatedEntity() {
    companion object {
        fun create(
            userId: Long,
            complexId: Long,
            unitTypeId: Long? = null,
        ): ApartmentSubscriptionEntity =
            ApartmentSubscriptionEntity(
                userId = userId,
                complexId = complexId,
                unitTypeId = unitTypeId,
            )
    }
}
