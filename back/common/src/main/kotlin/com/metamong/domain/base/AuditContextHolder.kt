package com.metamong.domain.base

/**
 * AuditProvider를 전역적으로 관리하는 홀더 클래스
 * 각 애플리케이션 모듈에서 적절한 AuditProvider 구현체를 설정한다.
 */
object AuditContextHolder {
    private var auditProvider: AuditProvider? = null

    /**
     * AuditProvider를 설정한다.
     */
    fun setAuditProvider(provider: AuditProvider) {
        auditProvider = provider
    }

    /**
     * 현재 설정된 AuditProvider를 반환한다.
     */
    fun getAuditProvider(): AuditProvider? = auditProvider

    /**
     * 현재 사용자 ID를 반환한다.
     * AuditProvider가 설정되지 않은 경우 null을 반환한다.
     */
    fun getCurrentUserId(): String? = auditProvider?.getCurrentUserId()
}
