package com.metamong.infra.persistence.user.repository

import com.metamong.base.BaseJpaBehaviorSpec
import com.metamong.domain.user.model.SocialProvider
import com.metamong.domain.user.model.UserEntity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class UserRepositoryTest(
    private val userRepository: UserRepository,
) : BaseJpaBehaviorSpec({

        Given("사용자가 저장되어 있을 때") {
            val user =
                UserEntity.create(
                    email = "test@example.com",
                    nickname = "테스트유저",
                    provider = SocialProvider.KAKAO,
                    providerId = "kakao-123",
                )
            userRepository.save(user)

            When("이메일로 조회하면") {
                val found = userRepository.findByEmail("test@example.com")

                Then("해당 사용자가 반환된다") {
                    found shouldNotBe null
                    found?.email shouldBe "test@example.com"
                    found?.nickname shouldBe "테스트유저"
                    found?.kakaoId shouldBe "kakao-123"
                }
            }

            When("존재하지 않는 이메일로 조회하면") {
                val found = userRepository.findByEmail("notfound@example.com")

                Then("null이 반환된다") {
                    found shouldBe null
                }
            }
        }
    })
