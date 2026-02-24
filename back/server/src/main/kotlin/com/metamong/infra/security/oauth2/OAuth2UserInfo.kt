package com.metamong.infra.security.oauth2

import com.metamong.domain.user.model.SocialProvider

sealed class OAuth2UserInfo(
    val attributes: Map<String, Any>,
) {
    abstract val providerId: String
    abstract val email: String?
    abstract val name: String?
    abstract val provider: SocialProvider
}

class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(attributes) {
    override val providerId: String
        get() = attributes["id"].toString()

    @Suppress("UNCHECKED_CAST")
    private val kakaoAccount: Map<String, Any>?
        get() = attributes["kakao_account"] as? Map<String, Any>

    @Suppress("UNCHECKED_CAST")
    private val profile: Map<String, Any>?
        get() = kakaoAccount?.get("profile") as? Map<String, Any>

    override val email: String?
        get() {
            val isVerified = kakaoAccount?.get("is_email_verified") as? Boolean ?: false
            return if (isVerified) kakaoAccount?.get("email") as? String else null
        }

    override val name: String?
        get() = profile?.get("nickname") as? String

    override val provider: SocialProvider = SocialProvider.KAKAO
}

class NaverOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(attributes) {
    @Suppress("UNCHECKED_CAST")
    private val response: Map<String, Any>?
        get() = attributes["response"] as? Map<String, Any>

    override val providerId: String
        get() = response?.get("id") as? String ?: ""

    override val email: String?
        get() = response?.get("email") as? String

    override val name: String?
        get() = response?.get("name") as? String

    override val provider: SocialProvider = SocialProvider.NAVER
}

class GoogleOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(attributes) {
    override val providerId: String
        get() = attributes["sub"] as? String ?: ""

    override val email: String?
        get() {
            val isVerified = attributes["email_verified"] as? Boolean ?: false
            return if (isVerified) attributes["email"] as? String else null
        }

    override val name: String?
        get() = attributes["name"] as? String

    override val provider: SocialProvider = SocialProvider.GOOGLE
}
