package com.metamong.domain.subscription.exception

import com.metamong.common.exception.CustomException
import org.springframework.http.HttpStatus

sealed class SubscriptionException(
    status: HttpStatus,
    message: String,
    code: String? = null,
) : CustomException(status, message, code) {
    class NotFound(
        message: String = "구독을 찾을 수 없습니다.",
    ) : SubscriptionException(HttpStatus.NOT_FOUND, message, "SUBSCRIPTION_001")

    class LimitExceeded(
        message: String = "구독은 최대 20개까지 가능합니다.",
    ) : SubscriptionException(HttpStatus.BAD_REQUEST, message, "SUBSCRIPTION_002")

    class AccessDenied(
        message: String = "해당 구독에 접근 권한이 없습니다.",
    ) : SubscriptionException(HttpStatus.FORBIDDEN, message, "SUBSCRIPTION_003")

    class InvalidRegionCode(
        message: String = "유효하지 않은 지역코드입니다.",
    ) : SubscriptionException(HttpStatus.BAD_REQUEST, message, "SUBSCRIPTION_005")

    class InvalidPriceRange(
        message: String = "최소 가격이 최대 가격보다 클 수 없습니다.",
    ) : SubscriptionException(HttpStatus.BAD_REQUEST, message, "SUBSCRIPTION_006")

    class ConditionRequired(
        message: String = "CONDITION 타입은 최소 1개의 조건이 필요합니다.",
    ) : SubscriptionException(HttpStatus.BAD_REQUEST, message, "SUBSCRIPTION_007")
}
