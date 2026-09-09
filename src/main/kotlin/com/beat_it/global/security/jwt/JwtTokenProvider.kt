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
    @Value("\${jwt.expiration}") val accessTokenValidity: Long,
    @Value("\${jwt.refresh-expiration}") val refreshTokenValidity: Long,
    @Value("\${jwt.refresh-expiration-short:604800000}") val refreshTokenShortValidity: Long,
    private val userDetailsService: UserDetailsService
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

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

    fun createRefreshToken(userId: String, rememberMe: Boolean = true): String {
        val now = Date()
        val validityDuration = getRefreshTokenValidity(rememberMe)
        val validity = Date(now.time + validityDuration)

        return Jwts.builder()
            .subject(userId)
            .claim("rememberMe", rememberMe)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun getRefreshTokenValidity(rememberMe: Boolean = true): Long {
        return if (rememberMe) refreshTokenValidity else refreshTokenShortValidity
    }

    fun getRememberMe(token: String): Boolean {
        return parseClaims(token)["rememberMe"] as? Boolean ?: true
    }

    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUserId(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    fun getUserId(token: String): String {
        return parseClaims(token).subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

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