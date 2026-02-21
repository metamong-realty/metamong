package com.metamong.base

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * kotest 를 활용한 integration 테스트를 위한 base class
 * 트랜잭션 범위 : given
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BaseIntegrationBehaviorSpec(
    body: BehaviorSpec.() -> Unit = {},
) : BehaviorSpec(body)
