package com.metamong.infra.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtAuthenticationToken(
    val userId: Long,
    val email: String,
) : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_USER"))) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): Long = userId
}
