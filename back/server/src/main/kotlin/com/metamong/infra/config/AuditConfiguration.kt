package com.metamong.infra.config

import com.metamong.domain.base.AuditContextHolder
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

/**
 * Audit 설정 클래스
 * 애플리케이션 시작 시 AuditProvider를 AuditContextHolder에 등록한다.
 */
@Component
class AuditConfiguration(
    private val securityAuditProvider: SecurityAuditProvider,
) {
    @PostConstruct
    fun configureAudit() {
        AuditContextHolder.setAuditProvider(securityAuditProvider)
    }
}
