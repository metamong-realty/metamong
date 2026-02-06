package com.metamong.infra.config

import com.metamong.domain.base.AuditProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Spring Security 기반 감사 정보 제공자
 * SecurityContextHolder를 사용하여 현재 사용자 정보를 제공한다.
 */
@Component
class SecurityAuditProvider : AuditProvider {
    
    override fun getCurrentUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication?.isAuthenticated == true && authentication.name != "anonymousUser") {
            // TODO: metamong의 인증 체계에 맞게 수정 필요
            return authentication.name
        }
        return null
    }
}