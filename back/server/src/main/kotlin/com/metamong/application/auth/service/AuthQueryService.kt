package com.metamong.application.auth.service

import com.metamong.application.auth.response.UserMeResponse
import com.metamong.domain.user.exception.UserException
import com.metamong.infra.persistence.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthQueryService(
    private val userRepository: UserRepository,
) {
    fun getMe(userId: Long): UserMeResponse {
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw UserException.NotFound()
        return UserMeResponse.from(user)
    }
}
