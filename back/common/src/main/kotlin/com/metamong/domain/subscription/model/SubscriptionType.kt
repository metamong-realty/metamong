package com.metamong.domain.subscription.model

enum class SubscriptionType(
    val priority: Int,
) {
    COMPLEX(1),
    CONDITION(2),
    REGION(3),
}
