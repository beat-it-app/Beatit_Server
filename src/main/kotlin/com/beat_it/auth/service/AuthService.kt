package com.beat_it.auth.service

import com.beat_it.auth.dto.LoginRequest
import com.beat_it.auth.dto.LoginResponse
import com.beat_it.auth.dto.SignUpRequest
import com.beat_it.auth.dto.SignUpResponse
import com.beat_it.auth.dto.SocialLoginRequest
import com.beat_it.auth.entity.UserAuthAccounts
import com.beat_it.auth.entity.UserSettings
import com.beat_it.auth.entity.Users
import com.beat_it.auth.entity.enum.AccountStatus
import com.beat_it.auth.entity.enum.Role
import com.beat_it.auth.entity.enum.SocialProvider
import com.beat_it.auth.repository.UserAuthAccountRepository
import com.beat_it.auth.repository.UserProfilesRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.auth.repository.UserSettingsRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.security.jwt.JwtTokenProvider
import com.beat_it.global.util.DateTimeUtil
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.data.redis.core.StringRedisTemplate
import org.slf4j.LoggerFactory

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userProfilesRepository: UserProfilesRepository,
    private val googleAuthService: GoogleAuthService,
    private val refreshTokenService: RefreshTokenService,
    private val emailService: EmailService,
    private val redisTemplate: StringRedisTemplate
){
    private val log = LoggerFactory.getLogger(AuthService::class.java)
    @Transactional
    fun signUp(dto : SignUpRequest): SignUpResponse {
        val identifier = dto.identifier
        checkDuplicateIdentifier(identifier)

        val email = dto.email
        if (userAuthAccountRepository.existsByEmail(email)) {
            throw BusinessException(ErrorCode.EMAIL_DUPLICATED)
        }

        val verifiedKey = "email:verified:$email"
        val isVerified = redisTemplate.opsForValue().get(verifiedKey)
        if (isVerified != "true") {
            throw BusinessException(ErrorCode.EMAIL_NOT_VERIFIED)
        }

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

        val password = dto.password
        val encodedPassword = passwordEncoder.encode(password)

        val userAuthAccount = UserAuthAccounts.createNormalUser(
            user = user,
            identifier = identifier,
            password = encodedPassword,
            email = dto.email
        )

        userAuthAccountRepository.save(userAuthAccount)

        redisTemplate.delete(verifiedKey)

        return SignUpResponse(
            userId = user.userId!!,
            identifier = userAuthAccount.identifier!!,
            email = userAuthAccount.email,
            createdAt = DateTimeUtil.format(user.createdAt),
        )
    }

    fun login(loginRequest: LoginRequest) : Triple<String, String, LoginResponse> {
        val userAuthAccount = userAuthAccountRepository.findByIdentifier(loginRequest.identifier)
            ?: throw BusinessException(ErrorCode.IDENTIFIER_NOT_FOUND)

        if (!passwordEncoder.matches(loginRequest.password, userAuthAccount.password)) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD)
        }

        val user = userAuthAccount.user
        val isCreatedProfile = userProfilesRepository.existsByUser_UserId(user.userId)

        val accessToken = jwtTokenProvider.createAccessToken(
            userId = user.userId.toString(),
            role = user.role
        )

        val refreshToken = jwtTokenProvider.createRefreshToken(user.userId.toString())
        refreshTokenService.saveRefreshToken(
            userId = user.userId.toString(),
            refreshToken = refreshToken,
            expirationMs = jwtTokenProvider.refreshTokenValidity
        )

        return Triple(
            accessToken,
            refreshToken,
            LoginResponse(
                userId = user.userId,
                role = user.role,
                isCreatedProfile = isCreatedProfile,
                socialProvider = userAuthAccount.socialProvider
            )
        )
    }

    fun checkDuplicateIdentifier(identifier: String): Boolean {
        if (userAuthAccountRepository.findByIdentifier(identifier) != null) {
            throw BusinessException(ErrorCode.IDENTIFIER_DUPLICATED)
        } else {
            return true
        }
    }

    @Transactional
    fun googleLogin(dto: SocialLoginRequest): Triple<String, String, LoginResponse> {
        val googlePayload = googleAuthService.verifyToken(dto.idToken)

        var userAuthAccount = userAuthAccountRepository.findByGoogleId(googlePayload.googleId)

        if (userAuthAccount == null) {
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

            userAuthAccount = UserAuthAccounts.createSocialUser(
                user = user,
                email = googlePayload.email,
                socialId = googlePayload.googleId,
                provider = SocialProvider.GOOGLE
            )
            userAuthAccountRepository.save(userAuthAccount)
        }

        val user = userAuthAccount.user
        val isCreatedProfile = userProfilesRepository.existsByUser_UserId(user.userId)

        val accessToken = jwtTokenProvider.createAccessToken(
            userId = user.userId.toString(),
            role = user.role
        )

        val refreshToken = jwtTokenProvider.createRefreshToken(user.userId.toString())
        refreshTokenService.saveRefreshToken(
            userId = user.userId.toString(),
            refreshToken = refreshToken,
            expirationMs = jwtTokenProvider.refreshTokenValidity
        )

        return Triple(
            accessToken,
            refreshToken,
            LoginResponse(
                userId = user.userId,
                role = user.role,
                isCreatedProfile = isCreatedProfile,
                socialProvider = SocialProvider.GOOGLE
            )
        )
    }

    fun reissue(refreshToken: String): Pair<String, String> {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN)
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val savedToken = refreshTokenService.getRefreshToken(userId)
            ?: throw BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        if (savedToken != refreshToken) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val user = userRepository.findById(userId.toLongOrNull() ?: throw BusinessException(ErrorCode.USER_NOT_FOUND))
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val newAccessToken = jwtTokenProvider.createAccessToken(userId, user.role)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(userId)

        refreshTokenService.saveRefreshToken(
            userId = userId,
            refreshToken = newRefreshToken,
            expirationMs = jwtTokenProvider.refreshTokenValidity
        )

        return Pair(newAccessToken, newRefreshToken)
    }

    fun sendEmailVerificationCode(email: String): Boolean {
        if (userAuthAccountRepository.existsByEmail(email)) {
            throw BusinessException(ErrorCode.EMAIL_DUPLICATED)
        }

        val verificationCode = generateVerificationCode()

        val codeKey = "email:code:$email"
        redisTemplate.opsForValue().set(codeKey, verificationCode, java.time.Duration.ofSeconds(180))

        try {
            emailService.sendVerificationEmail(email, verificationCode)
        } catch (e: Exception) {
            log.error("Failed to send email verification code to $email", e)
            redisTemplate.delete(codeKey)
            throw BusinessException(ErrorCode.EMAIL_SEND_FAILED)
        }
        return true
    }

    private fun generateVerificationCode(): String {
        val charPool = "0123456789"
        return (1..6)
            .map { charPool.random() }
            .joinToString("")
    }

    fun verifyEmailVerificationCode(email: String, code: String): Boolean {
        val codeKey = "email:code:$email"
        val storedCode = redisTemplate.opsForValue().get(codeKey)
            ?: throw BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED)

        if (storedCode == code) {
            redisTemplate.delete(codeKey)
            val verifiedKey = "email:verified:$email"
            redisTemplate.opsForValue().set(verifiedKey, "true", java.time.Duration.ofSeconds(600))
            return true
        } else {
            throw BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH)
        }
    }
}