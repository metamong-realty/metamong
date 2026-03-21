package com.metamong.domain.subscription.model

enum class NotificationStatus {
    PENDING,  // 알림 대기 (읽지 않음)
    SENT,     // 외부 발송 완료 (이메일 등)
    READ,     // 사용자가 읽음
    FAILED,   // 발송 실패
}
