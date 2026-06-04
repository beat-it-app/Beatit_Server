package com.beat_it.global.security.jwt

import com.beat_it.auth.entity.enum.Role
import com.beat_it.auth.service.UserDetailsService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val accessTokenValidity: Long,
    private val userDetailsService: UserDetailsService
) {
    // 1. 시크릿 키 객체 생성 (JJWT 최신 버전 방식)
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    // 2. Access Token 발급 (subject에 userId를 넣습니다)
    fun createAccessToken(userId: String, role: Role): String {
        val now = Date()
        val validity = Date(now.time + accessTokenValidity)

        return Jwts.builder()
            .subject(userId)
            .claim("role", role)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    // 3. 토큰에서 Authentication 객체 추출 (필터에서 사용)
    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUserId(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    // 4. 토큰에서 userId(subject) 추출
    fun getUserId(token: String): String {
        return parseClaims(token).subject
    }

    // 5. 토큰 유효성 검증
    fun validateToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    // 토큰 파싱 유틸리티
    private fun parseClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            e.claims
        }
    }
}