package com.beat_it.global.security.jwt

import org.springframework.util.StringUtils
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Request Header에서 토큰 추출
        val token = resolveToken(request)

        // 2. 토큰 유효성 검사 및 SecurityContext에 인증 정보 저장
        if (token != null && jwtTokenProvider.validateToken(token)) {
            val authentication = jwtTokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    // 헤더에서 "Bearer " 접두사를 제거하고 토큰만 추출
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (!StringUtils.hasText(bearerToken)) return null

        if (bearerToken.startsWith("Bearer ", ignoreCase = true)) {
            val token = bearerToken.substring(7).trim()
            return if (token.startsWith("Bearer ", ignoreCase = true)) token.substring(7).trim() else token
        }

        if (bearerToken.trim().startsWith("eyJ")) {
            return bearerToken.trim()
        }
        
        return null
    }
}