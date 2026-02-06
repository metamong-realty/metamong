package com.metamong.domain.base.lecagy.model

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * LegacyBaseEntity 클래스
 * 1.5 버전에서 사용하던 DB 컨벤션으로 구성된 클래스이다.
 * reg_id, mod_id, reg_date, mod_date 컬럼을 가지고 있는 테이블에 적용한다.
 * 앞으로 생성되는 Table에서는 BaseEntity를 사용한다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class LegacyBaseEntity : LegacyBaseTimeEntity() {
    @CreatedBy
    @Column(name = "reg_id")
    var createdBy: Long? = null

    @LastModifiedBy
    @Column(name = "mod_id")
    var updatedBy: Long? = null
}
