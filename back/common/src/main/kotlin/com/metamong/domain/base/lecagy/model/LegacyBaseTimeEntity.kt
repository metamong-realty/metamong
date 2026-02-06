package com.metamong.domain.base.lecagy.model

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class LegacyBaseTimeEntity(
    @Column(name = "reg_date")
    var createdAt: LocalDateTime? = null,
    @Column(name = "mod_date")
    var updatedAt: LocalDateTime? = null,
) {
    @PrePersist
    open fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    open fun preUpdate() {
        val now = LocalDateTime.now()
        updatedAt = now
    }
}
