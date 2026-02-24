package com.metamong.infra.security.oauth2

import com.metamong.domain.auth.exception.OAuthException

object OAuth2UserInfoFactory {
    fun create(
        registrationId: String,
        attributes: Map<String, Any>,
    ): OAuth2UserInfo =
        when (registrationId) {
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            "naver" -> NaverOAuth2UserInfo(attributes)
            "google" -> GoogleOAuth2UserInfo(attributes)
            else -> throw OAuthException.UnsupportedProvider()
        }
}
