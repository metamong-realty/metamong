package com.metamong.base

import com.metamong.config.JpaTestConfig
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor

/**
 * kotest 를 활용한 jpa integration 테스트를 위한 base class
 * 트랜잭션 범위 : given
 */
@DataJpaTest
@ContextConfiguration(classes = [JpaTestConfig::class])
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BaseJpaBehaviorSpec(
    body: BehaviorSpec.() -> Unit = {},
) : BehaviorSpec(body)
