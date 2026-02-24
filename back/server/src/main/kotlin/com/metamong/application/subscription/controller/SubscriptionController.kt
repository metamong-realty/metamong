package com.metamong.application.subscription.controller

import com.metamong.application.subscription.request.CreateSubscriptionRequest
import com.metamong.application.subscription.request.UpdateSubscriptionRequest
import com.metamong.application.subscription.response.SubscriptionResponse
import com.metamong.application.subscription.service.SubscriptionCommandService
import com.metamong.application.subscription.service.SubscriptionQueryService
import com.metamong.common.response.ApiResponse
import com.metamong.infra.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/subscriptions")
@Tag(name = "구독 API", description = "거래 알림 구독 관리 API")
class SubscriptionController(
    private val subscriptionCommandService: SubscriptionCommandService,
    private val subscriptionQueryService: SubscriptionQueryService,
) {
    @Operation(summary = "구독 생성")
    @PostMapping
    fun create(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: CreateSubscriptionRequest,
    ): ApiResponse<SubscriptionResponse> {
        val result = subscriptionCommandService.create(userId, request.toDto())
        return ApiResponse.created(result)
    }

    @Operation(summary = "내 구독 목록 조회")
    @GetMapping
    fun getMySubscriptions(
        @CurrentUser userId: Long,
    ): ApiResponse<List<SubscriptionResponse>> {
        val result = subscriptionQueryService.getMySubscriptions(userId)
        return ApiResponse.ok(result)
    }

    @Operation(summary = "구독 상세 조회")
    @GetMapping("/{id}")
    fun getSubscription(
        @CurrentUser userId: Long,
        @PathVariable id: Long,
    ): ApiResponse<SubscriptionResponse> {
        val result = subscriptionQueryService.getSubscription(id, userId)
        return ApiResponse.ok(result)
    }

    @Operation(summary = "구독 수정")
    @PutMapping("/{id}")
    fun update(
        @CurrentUser userId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSubscriptionRequest,
    ): ApiResponse<SubscriptionResponse> {
        val result = subscriptionCommandService.update(id, userId, request.toDto())
        return ApiResponse.ok(result)
    }

    @Operation(summary = "구독 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @CurrentUser userId: Long,
        @PathVariable id: Long,
    ) {
        subscriptionCommandService.delete(id, userId)
    }
}
