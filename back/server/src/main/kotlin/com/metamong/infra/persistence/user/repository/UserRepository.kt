package com.metamong.infra.persistence.user.repository

import com.metamong.domain.user.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?

    fun findByKakaoId(kakaoId: String): UserEntity?

    fun findByNaverId(naverId: String): UserEntity?

    fun findByGoogleId(googleId: String): UserEntity?
}
