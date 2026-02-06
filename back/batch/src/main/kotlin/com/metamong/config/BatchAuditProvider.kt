package com.metamong.config

import com.metamong.domain.base.AuditProvider
import org.springframework.stereotype.Component

/**
 * 배치 애플리케이션용 감사 정보 제공자
 * 배치 작업에서는 시스템 사용자로 고정한다.
 */
@Component
class BatchAuditProvider : AuditProvider {
    override fun getCurrentUserId(): String = "BATCH_SYSTEM"
}
