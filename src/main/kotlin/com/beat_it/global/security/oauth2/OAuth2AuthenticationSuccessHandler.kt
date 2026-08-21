package com.beat_it.global.security.oauth2

import com.beat_it.auth.repository.UserAuthAccountRepository
import com.beat_it.global.security.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    @Value("\${app.oauth2.authorized-redirect-uri:http://localhost:3000/oauth2/redirect}")
    private val authorizedRedirectUri: String
) : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)

        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ): String {
        val oAuth2User = authentication!!.principal as OAuth2User
        
        // 구글의 고유 sub (Google ID) 값 획득
        val googleId = oAuth2User.attributes["sub"] as? String 
            ?: throw IllegalArgumentException("Google ID (sub) not found in OAuth2 user attributes")

        // 데이터베이스에서 Google ID로 안전하게 사용자 계정 조회
        val userAuthAccount = userAuthAccountRepository.findByGoogleId(googleId)
            ?: throw IllegalStateException("User not registered in database for Google ID: $googleId")

        val user = userAuthAccount.user
        val userId = user.userId.toString()
        val role = user.role

        // JWT 토큰 생성
        val accessToken = jwtTokenProvider.createAccessToken(userId, role)

        // 프론트엔드 리다이렉트 타겟 URL 빌드 (쿼리 파라미터에 토큰을 싣고 전송)
        return UriComponentsBuilder.fromUriString(authorizedRedirectUri)
            .queryParam("token", accessToken)
            .build().toUriString()
    }
}
