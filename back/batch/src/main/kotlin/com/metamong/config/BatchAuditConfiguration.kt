package com.metamong.config

import com.metamong.domain.base.AuditContextHolder
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

/**
 * 배치 애플리케이션 Audit 설정 클래스
 * 애플리케이션 시작 시 BatchAuditProvider를 AuditContextHolder에 등록한다.
 */
@Component
class BatchAuditConfiguration(
    private val batchAuditProvider: BatchAuditProvider,
) {
    @PostConstruct
    fun configureAudit() {
        AuditContextHolder.setAuditProvider(batchAuditProvider)
    }
}
