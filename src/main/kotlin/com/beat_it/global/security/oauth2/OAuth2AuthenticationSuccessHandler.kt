package com.beat_it.global.security.oauth2

import com.beat_it.auth.repository.UserAuthAccountRepository
import com.beat_it.auth.service.RefreshTokenService
import com.beat_it.global.security.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    private val refreshTokenService: RefreshTokenService,
    @Value("\${app.oauth2.authorized-redirect-uri:http://localhost:3000/oauth2/redirect}")
    private val authorizedRedirectUri: String
) : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User
        
        // 구글의 고유 sub (Google ID) 값 획득
        val googleId = oAuth2User.attributes["sub"] as? String 
            ?: throw IllegalArgumentException("Google ID (sub) not found in OAuth2 user attributes")

        // 데이터베이스에서 Google ID로 안전하게 사용자 계정 조회
        val userAuthAccount = userAuthAccountRepository.findByGoogleId(googleId)
            ?: throw IllegalStateException("User not registered in database for Google ID: $googleId")

        val user = userAuthAccount.user
        val userId = user.userId.toString()
        val role = user.role

        // 1. JWT 토큰 생성
        val accessToken = jwtTokenProvider.createAccessToken(userId, role)
        val refreshToken = jwtTokenProvider.createRefreshToken(userId)

        // Redis에 Refresh Token 저장
        refreshTokenService.saveRefreshToken(
            userId = userId,
            refreshToken = refreshToken,
            expirationMs = jwtTokenProvider.refreshTokenValidity
        )

        // 2. Response Cookie 구워주기 (SameSite=Lax, HttpOnly, Path=/)
        val accessCookie = ResponseCookie.from("access_token", accessToken)
            .path("/")
            .httpOnly(true)
            .secure(false) // HTTPS 설정 시 true로 전환 권장
            .maxAge(jwtTokenProvider.accessTokenValidity / 1000)
            .sameSite("Lax")
            .build()
        
        val refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
            .path("/")
            .httpOnly(true)
            .secure(false) // HTTPS 설정 시 true로 전환 권장
            .maxAge(jwtTokenProvider.refreshTokenValidity / 1000)
            .sameSite("Lax")
            .build()
        
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

        // 3. 토큰이 노출되지 않는 깨끗한 URL로 리다이렉트
        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $authorizedRedirectUri")
            return
        }
        
        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, authorizedRedirectUri)
    }
}
