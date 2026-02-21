package com.metamong.base

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.test.context.ActiveProfiles

/**
 * kotest 를 활용한 controller 테스트를 위한 base class
 */
@ActiveProfiles("test")
open class BaseControllerBehaviorSpec(
    body: BehaviorSpec.() -> Unit = {},
) : BehaviorSpec(body)
