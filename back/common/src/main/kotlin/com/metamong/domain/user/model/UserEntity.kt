package com.metamong.domain.user.model

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    val email: String,
    var nickname: String,
    @Enumerated(EnumType.STRING)
    var status: UserStatus = UserStatus.ACTIVE,
    var kakaoId: String? = null,
    var naverId: String? = null,
    var googleId: String? = null,
) : BaseEntity() {
    fun linkSocialProvider(
        provider: SocialProvider,
        providerId: String,
    ) {
        when (provider) {
            SocialProvider.KAKAO -> kakaoId = providerId
            SocialProvider.NAVER -> naverId = providerId
            SocialProvider.GOOGLE -> googleId = providerId
        }
    }

    fun withdraw() {
        status = UserStatus.WITHDRAWN
    }

    fun changeNickname(newNickname: String) {
        nickname = newNickname
    }

    companion object {
        fun create(
            email: String,
            nickname: String,
            provider: SocialProvider,
            providerId: String,
        ): UserEntity {
            val user = UserEntity(email = email, nickname = nickname)
            user.linkSocialProvider(provider, providerId)
            return user
        }
    }
}
