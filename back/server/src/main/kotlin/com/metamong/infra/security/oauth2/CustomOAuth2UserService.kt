package com.metamong.infra.security.oauth2

import com.metamong.domain.user.exception.UserException
import com.metamong.domain.user.model.SocialProvider
import com.metamong.domain.user.model.UserEntity
import com.metamong.domain.user.model.UserStatus
import com.metamong.infra.persistence.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val registrationId = userRequest.clientRegistration.registrationId
        val userNameAttributeName =
            userRequest.clientRegistration
                .providerDetails.userInfoEndpoint.userNameAttributeName

        val userInfo = OAuth2UserInfoFactory.create(registrationId, oAuth2User.attributes)
        val email = userInfo.email ?: throw UserException.EmailRequired()
        val user = findOrCreateUser(userInfo, email)

        return CustomOAuth2User(user, oAuth2User.attributes, userNameAttributeName)
    }

    private fun findOrCreateUser(
        userInfo: OAuth2UserInfo,
        email: String,
    ): UserEntity {
        val existingByProvider = findByProvider(userInfo.provider, userInfo.providerId)
        if (existingByProvider != null) {
            check(existingByProvider.status != UserStatus.WITHDRAWN) {
                throw UserException.AlreadyWithdrawn()
            }
            return existingByProvider
        }

        val existingByEmail = userRepository.findByEmail(email)
        if (existingByEmail != null) {
            check(existingByEmail.status != UserStatus.WITHDRAWN) {
                throw UserException.AlreadyWithdrawn()
            }
            existingByEmail.linkSocialProvider(userInfo.provider, userInfo.providerId)
            return userRepository.save(existingByEmail)
        }

        val newUser =
            UserEntity.create(
                email = email,
                nickname = userInfo.name ?: email.substringBefore("@"),
                provider = userInfo.provider,
                providerId = userInfo.providerId,
            )
        return userRepository.save(newUser)
    }

    private fun findByProvider(
        provider: SocialProvider,
        providerId: String,
    ): UserEntity? =
        when (provider) {
            SocialProvider.KAKAO -> userRepository.findByKakaoId(providerId)
            SocialProvider.NAVER -> userRepository.findByNaverId(providerId)
            SocialProvider.GOOGLE -> userRepository.findByGoogleId(providerId)
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
