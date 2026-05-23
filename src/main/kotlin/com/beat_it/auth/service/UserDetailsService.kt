package com.beat_it.auth.service

import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserDetailsService (
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(publicId: String): UserDetails {
        val user = userRepository.findByPublicId(UUID.fromString(publicId))
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        return User(
            user.publicId.toString(),
            "",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }
}