package com.beat_it.global.security

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object SecurityUtil {
    fun getCurrentUserPublicId(): String {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || !authentication.isAuthenticated) {
            throw BusinessException(ErrorCode.UNAUTHORIZED)
        }

        return when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            is String -> principal
            else -> throw BusinessException(ErrorCode.UNAUTHORIZED)
        }
    }

    fun getHeaderUserPublicId(): String? {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return requestAttributes?.request?.getHeader("X-User-Public-Id")
    }

    fun getHeaderTeamPublicId(): String? {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return requestAttributes?.request?.getHeader("X-Team-Public-Id")
    }
}