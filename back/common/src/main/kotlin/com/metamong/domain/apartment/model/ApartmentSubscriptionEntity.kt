package com.metamong.domain.apartment.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "apartment_subscriptions")
class ApartmentSubscriptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    val complexId: Long,
    val unitTypeId: Long? = null,
    var createdAt: LocalDateTime? = null,
    var createdBy: String? = null,
) {
    @PrePersist
    fun prePersist() {
        createdAt = createdAt ?: LocalDateTime.now()
        createdBy = createdBy ?: "WEBAPP:$userId"
    }

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