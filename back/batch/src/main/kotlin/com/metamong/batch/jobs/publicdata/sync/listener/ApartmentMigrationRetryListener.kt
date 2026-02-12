package com.metamong.batch.jobs.publicdata.sync.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.stereotype.Component

@Component
class ApartmentMigrationRetryListener : RetryListener {

    override fun <T, E : Throwable> open(context: RetryContext, callback: RetryCallback<T, E>): Boolean {
        return true
    }

    override fun <T, E : Throwable> close(
        context: RetryContext,
        callback: RetryCallback<T, E>,
        throwable: Throwable?
    ) {
        val retryCount = context.retryCount
        if (retryCount > 0) {
            if (throwable == null) {
                logger.info { "재시도 성공: ${retryCount}회 재시도 후 성공" }
            } else {
                logger.warn { "재시도 최종 실패: ${retryCount}회 재시도 후 실패 - ${throwable.javaClass.simpleName}: ${throwable.message}" }
            }
        }
    }

    override fun <T, E : Throwable> onError(
        context: RetryContext,
        callback: RetryCallback<T, E>,
        throwable: Throwable
    ) {
        val retryCount = context.retryCount
        val maxAttempts = 3 // retryLimit과 동일하게 설정
        
        logger.warn { 
            "재시도 ${retryCount}/${maxAttempts}회 실패: " +
            "${throwable.javaClass.simpleName} - ${throwable.message}"
        }
        
        // 상세 스택 트레이스는 DEBUG 레벨에서만
        logger.debug(throwable) { "재시도 실패 상세 정보" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}