package com.beat_it.auth.service

//import com.beat_it.auth.dto.LoginRequest
import com.beat_it.auth.dto.SignUpRequest
import com.beat_it.auth.dto.SignUpResponse
import com.beat_it.auth.entity.UserAuthAccounts
import com.beat_it.auth.entity.UserSettings
import com.beat_it.auth.entity.Users
import com.beat_it.auth.entity.enum.AccountStatus
import com.beat_it.auth.entity.enum.Role
import com.beat_it.auth.repository.UserAuthAccountRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.auth.repository.UserSettingsRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.security.jwt.JwtTokenProvider
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class UserService (
    private val userRepository: UserRepository,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
){
    @Transactional
    fun signUp(dto : SignUpRequest): SignUpResponse {

        // 1. Users 생성 및 저장
        var user = Users.createNewUser(
            role = Role.USER,
            accountStatus = AccountStatus.ACTIVE,
            createdAt = OffsetDateTime.now()
        )
        user = userRepository.save(user)

        // 2. UserSettings 생성 및 저장
        val userSetting = UserSettings.createNewUser(
            user,
            allowAutoLogin = false,
            updatedAt = OffsetDateTime.now()
        )
        userSettingsRepository.save(userSetting)

        // 3. UserAuthAccounts 생성 및 저장
        val socialId = dto.socialId
        val userAuthAccount = if (socialId == null) {
            val identifier = dto.identifier ?: throw BusinessException(ErrorCode.MISSING_IDENTIFIER)
            val password = dto.password ?: throw BusinessException(ErrorCode.MISSING_PASSWORD)
            val encodedPassword = passwordEncoder.encode(password)

            UserAuthAccounts.createNormalUser( // 일반 가입
                user = user,
                identifier = identifier,
                password = encodedPassword ?: throw RuntimeException("비밀번호 암호화에 실패했습니다."),
                email = dto.email
            )
        } else {
            val provider = dto.provider ?: throw BusinessException(ErrorCode.MISSING_PROVIDER)

            UserAuthAccounts.createSocialUser( // 소셜 가입
                user = user,
                email = dto.email,
                socialId = socialId,
                provider = provider
            )
        }
        userAuthAccountRepository.save(userAuthAccount)

        val accessToken = jwtTokenProvider.createAccessToken(
            publicId = user.publicId.toString(),
            role = user.role
        )

        return SignUpResponse(
            publicId = user.publicId,
            identifier = userAuthAccount.identifier,
            email = userAuthAccount.email,
            createdAt = user.createdAt,
            accessToken = accessToken
        )
    }

    // 2. 로그인
//    fun login(loginRequest: LoginRequest) {
//
//    }

    // 4. 아이디 중복 확인
    fun checkDuplicateIdentifier(identifier: String): Boolean {
        if (userAuthAccountRepository.findByIdentifier(identifier) != null) {
            throw BusinessException(ErrorCode.IDENTIFIER_DUPLICATED)
        } else {
            return true
        }
    }

    // 5. 이메일 인증번호 발송
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

    // 6. 이메일 인증번호 인증하기
    fun verifyEmailVerificationCode(email: String, code: String): Boolean {
        val storedCode = "123456" // fixme 예시로 고정된 코드, 실제로는 DB나 캐시에서 조회해야 함
        if (storedCode == code) {
            // todo 인증 성공 시 필요한 추가 로직 (예: 인증 상태 업데이트)
            return true
        } else {
            throw BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH)
        }
    }

    // Todo : 로그인 된 사용자 헤더로 반환할 수 있도록 하는? 함수 global로 설정해서 하기
//    val getUser(){
//
//    }
}