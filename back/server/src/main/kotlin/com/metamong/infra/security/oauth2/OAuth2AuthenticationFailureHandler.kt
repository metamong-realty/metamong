package com.metamong.infra.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler(
    @Value("\${app.oauth2.redirect-uri}") private val redirectUri: String,
) : SimpleUrlAuthenticationFailureHandler() {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val targetUrl =
            UriComponentsBuilder
                .fromUriString(redirectUri)
                .queryParam("error", exception.localizedMessage)
                .build()
                .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
