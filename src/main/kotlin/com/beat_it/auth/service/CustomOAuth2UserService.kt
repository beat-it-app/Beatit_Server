package com.beat_it.auth.service

import com.beat_it.auth.entity.UserAuthAccounts
import com.beat_it.auth.entity.UserSettings
import com.beat_it.auth.entity.Users
import com.beat_it.auth.entity.enum.AccountStatus
import com.beat_it.auth.entity.enum.Role
import com.beat_it.auth.entity.enum.SocialProvider
import com.beat_it.auth.repository.UserAuthAccountRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.auth.repository.UserSettingsRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    private val userSettingsRepository: UserSettingsRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val delegate = DefaultOAuth2UserService()

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = delegate.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
        val attributes = oAuth2User.attributes

        val (socialId, email, provider) = extractSocialProfile(registrationId, attributes)

        // 1. 소셜 ID로 기존 계정 조회 또는 신규 회원가입 진행
        val userAuthAccount = findOrCreateUser(socialId, email, provider)
        val user = userAuthAccount.user

        // 2. 인증 성공 후 성공 핸들러에서 참조할 수 있도록 userId를 attributes에 병합
        val customAttributes = attributes.toMutableMap()
        customAttributes["userId"] = user.userId.toString()
        customAttributes["role"] = user.role.name

        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")),
            customAttributes,
            userNameAttributeName
        )
    }

    private fun extractSocialProfile(registrationId: String, attributes: Map<String, Any>): Triple<String, String, SocialProvider> {
        return when (registrationId) {
            "google" -> {
                val socialId = attributes["sub"] as? String ?: throw IllegalArgumentException("Google sub is missing")
                val email = attributes["email"] as? String ?: ""
                Triple(socialId, email, SocialProvider.GOOGLE)
            }
            else -> throw IllegalArgumentException("Unsupported OAuth provider: $registrationId")
        }
    }

    private fun findOrCreateUser(socialId: String, email: String, provider: SocialProvider): UserAuthAccounts {
        val existingAccount = when (provider) {
            SocialProvider.GOOGLE -> userAuthAccountRepository.findByGoogleId(socialId)
            else -> throw IllegalArgumentException("Unsupported OAuth provider: $provider")
        }

        if (existingAccount != null) {
            return existingAccount
        }

        // 신규 회원 가입 처리
        var user = Users.createNewUser(
            role = Role.USER,
            accountStatus = AccountStatus.ACTIVE
        )
        user = userRepository.save(user)

        val userSetting = UserSettings.createNewUser(
            user,
            allowAutoLogin = false
        )
        userSettingsRepository.save(userSetting)

        val userAuthAccount = UserAuthAccounts.createSocialUser(
            user = user,
            email = email,
            socialId = socialId,
            provider = provider
        )
        return userAuthAccountRepository.save(userAuthAccount)
    }
}
