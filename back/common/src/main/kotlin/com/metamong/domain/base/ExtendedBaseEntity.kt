package com.metamong.domain.base

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * ExtendedBaseEntity 클래스
 * createdBy, updatedBy, createdAt, updatedAt 컬럼을 가지고 있는 테이블에 적용한다.
 * weolbu-backend-webapp에서 이관된 도메인에서 사용한다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class ExtendedBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var createdBy: String? = null
    var updatedBy: String? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @PrePersist
    fun prePersist() {
        val userId = getCurrentUserId() ?: "unknown"
        createdBy = "WEBAPP:$userId"
        updatedBy = "WEBAPP:$userId"

        val now = LocalDateTime.now()
        createdAt = createdAt ?: LocalDateTime.now()
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        val userId = getCurrentUserId() ?: "unknown"
        updatedBy = "WEBAPP:$userId"
        updatedAt = LocalDateTime.now()
    }

    private fun getCurrentUserId(): String? = AuditContextHolder.getCurrentUserId()
}
