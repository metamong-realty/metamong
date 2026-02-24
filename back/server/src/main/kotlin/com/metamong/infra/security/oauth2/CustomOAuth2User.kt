package com.metamong.infra.security.oauth2

import com.metamong.domain.user.model.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(
    val userEntity: UserEntity,
    private val attributes: Map<String, Any>,
    private val nameAttributeKey: String,
) : OAuth2User {
    override fun getName(): String = attributes[nameAttributeKey]?.toString() ?: ""

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_USER"))
}
