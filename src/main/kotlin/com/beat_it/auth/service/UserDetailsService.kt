package com.beat_it.auth.service

import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsService (
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(userId: String): UserDetails {
        val user = userRepository.findByIdOrNull(userId.toLong())
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        return User(
            user.userId.toString(),
            "",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }
}