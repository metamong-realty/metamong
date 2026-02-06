package com.metamong.domain.base

/**
 * 감사(Audit) 정보 제공 인터페이스
 * 현재 사용자 정보를 제공하는 역할을 담당한다.
 * 각 애플리케이션 모듈에서 구체적인 구현을 제공한다.
 */
interface AuditProvider {
    /**
     * 현재 사용자의 ID를 반환한다.
     * 인증되지 않은 경우 null을 반환한다.
     */
    fun getCurrentUserId(): String?
}
