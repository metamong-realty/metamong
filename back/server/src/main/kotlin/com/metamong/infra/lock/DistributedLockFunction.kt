package com.metamong.infra.lock

import com.metamong.common.exception.CommonException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun <T> distributedLock(
    key: String,
    waitDuration: Duration = 5.seconds,
    leaseDuration: Duration = 3.seconds,
    function: () -> T,
): T {
    val rLock = DistributedLockExecutor.getLock(key)
    val acquired =
        rLock.tryLock(
            waitDuration.inWholeSeconds,
            leaseDuration.inWholeSeconds,
            TimeUnit.SECONDS,
        )
    if (!acquired) {
        throw CommonException.LockAcquisitionFailed()
    }
    return try {
        DistributedLockExecutor.processWithNewTransaction(function)
    } finally {
        runCatching { rLock.unlock() }
            .onFailure { e ->
                if (e is IllegalMonitorStateException) {
                    DistributedLockExecutor.logger.info { "Redisson Lock Already UnLock Key : $key" }
                }
            }
    }
}
