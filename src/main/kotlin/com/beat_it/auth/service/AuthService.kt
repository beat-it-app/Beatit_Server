package com.beat_it.auth.service

import com.beat_it.auth.dto.LoginRequest
import com.beat_it.auth.dto.LoginResponse
import com.beat_it.auth.dto.SignUpRequest
import com.beat_it.auth.dto.SignUpResponse
import com.beat_it.auth.entity.UserAuthAccounts
import com.beat_it.auth.entity.UserSettings
import com.beat_it.auth.entity.Users
import com.beat_it.auth.entity.enum.AccountStatus
import com.beat_it.auth.entity.enum.Role
import com.beat_it.auth.repository.UserAuthAccountRepository
import com.beat_it.auth.repository.UserProfilesRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.auth.repository.UserSettingsRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.security.jwt.JwtTokenProvider
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userProfilesRepository: UserProfilesRepository
){
    @Transactional
    fun signUp(dto : SignUpRequest): SignUpResponse {
        val identifier = dto.identifier ?: throw BusinessException(ErrorCode.MISSING_IDENTIFIER)
        val password = dto.password ?: throw BusinessException(ErrorCode.MISSING_PASSWORD)

        checkDuplicateIdentifier(identifier)

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

        val encodedPassword = passwordEncoder.encode(password)

        val userAuthAccount = UserAuthAccounts.createNormalUser(
            user = user,
            identifier = identifier,
            password = encodedPassword,
            email = dto.email
        )

        userAuthAccountRepository.save(userAuthAccount)

        return SignUpResponse(
            userId = user.userId,
            identifier = userAuthAccount.identifier,
            email = userAuthAccount.email,
            createdAt = user.createdAt
        )
    }

    fun login(loginRequest: LoginRequest) : Pair<String,LoginResponse> {
        val userAuthAccount = userAuthAccountRepository.findByIdentifier(loginRequest.identifier)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (!passwordEncoder.matches(loginRequest.password, userAuthAccount.password)) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD)
        }

        val user = userAuthAccount.user
        val isCreatedProfile = userProfilesRepository.existsByUser_UserId(user.userId)

        val accessToken = jwtTokenProvider.createAccessToken(
            userId = user.userId.toString(),
            role = user.role
        )

        return Pair(
            accessToken,
            LoginResponse(
                userId = user.userId,
                role = user.role,
                isCreatedProfile = isCreatedProfile
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

    fun sendEmailVerificationCode(email: String): Boolean {
        try {
            val verificationCode = generateVerificationCode()
            // emailService.send(email, verificationCode) 등 발송 로직
        } catch (e: Exception) {
            // 발송 실패 시 ErrorCode 활용
            throw BusinessException(ErrorCode.EMAIL_SEND_FAILED)
        }
        return true
    }

    private fun generateVerificationCode(): String {
        val charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { charPool.random() }
            .joinToString("")
    }

    fun verifyEmailVerificationCode(email: String, code: String): Boolean {
        val storedCode = "123456" // fixme 예시로 고정된 코드, 실제로는 DB나 캐시에서 조회해야 함
        if (storedCode == code) {
            // todo 인증 성공 시 필요한 추가 로직 (예: 인증 상태 업데이트)
            return true
        } else {
            throw BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH)
        }
    }
}