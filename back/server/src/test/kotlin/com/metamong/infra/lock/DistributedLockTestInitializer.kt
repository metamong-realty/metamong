package com.metamong.infra.lock

import io.mockk.every
import io.mockk.mockkStatic

object DistributedLockTestInitializer {
    fun mockExecutionByStatic() {
        mockkStatic("com.metamong.infra.lock.DistributedLockFunctionKt")
        every {
            distributedLock<Any?>(any(), any(), any(), captureLambda())
        } answers {
            val lambda: () -> Any? = arg<(() -> Any?)>(3)
            lambda()
        }
    }
}
