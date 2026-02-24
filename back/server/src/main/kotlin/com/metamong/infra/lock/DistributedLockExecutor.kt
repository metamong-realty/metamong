package com.metamong.infra.lock

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component

@Component
class DistributedLockExecutor(
    innerRedissonClient: RedissonClient,
    innerDistributedLockTransactionProcessor: DistributedLockTransactionProcessor,
) {
    init {
        redissonClient = innerRedissonClient
        distributedLockTransactionProcessor = innerDistributedLockTransactionProcessor
    }

    companion object {
        private const val LOCK_PREFIX = "lock:"

        val logger = KotlinLogging.logger { }

        private lateinit var redissonClient: RedissonClient
        private lateinit var distributedLockTransactionProcessor: DistributedLockTransactionProcessor

        fun getLock(key: String): RLock = redissonClient.getLock(LOCK_PREFIX + key)

        fun <T> processWithNewTransaction(function: () -> T): T = distributedLockTransactionProcessor.proceed(function)
    }
}
