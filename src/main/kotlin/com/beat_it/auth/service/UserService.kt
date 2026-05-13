package com.beat_it.auth.service

import com.beat_it.auth.dto.SignUpDtoRequest
import com.beat_it.auth.entity.UserSetting
import com.beat_it.auth.entity.Users
import com.beat_it.auth.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository : UserRepository,

){
    // 1. 회원가입 
    @Transactional
    fun signUp(dto : SignUpDtoRequest): SignUpDtoResponse {
        user = Users(
            accountStatus = AccountStatus.ACTIVE,
            createdAt = OffsetDateTime.now()
        )

        if (dto.socialId == null){ // 일반 회원가입인 경우
            userAuthAccount = userAuthAccounts(
                identifier = dto.identifier,
                password = dto.password,
                email = dto.email,
                user = user // 관계 설정
            )
        } else { // 소셜 회원가입인 경우
            userAuthAccount = userAuthAccounts(
                email = dto.email,

                kakaoId = if (dto.socialId.startsWith("kakao_")) dto.socialId else null,
                naverId = if (dto.socialId.startsWith("naver_")) dto.socialId else null,
                googleId = if (dto.socialId.startsWith("google_")) dto.socialId else null,
                user = user // 관계 설정
            )
        }

        userSetting = UserSetting(
            timezone = dto.timezone
        )

        userRepository.save(user, userSetting, userAuthAccount)

        return SignUpDtoResponse(
            publicId = user.publicId,
            identifier = userAuthAccount.identifier,
            email = userAuthAccount.email,
            timezone = userSetting.timezone
        )
    }

    // 4. 아이디 중복 확인
    fun checkDuplicateIdentifier(identifier: String): Boolean {
        val existingUserAuthAccount = userRepository.findByIdentifier(identifier)
        
        return existingUserAuthAccount != null
    }

    // 5. 이메일 인증번호 발송
    fun sendEmailVerificationCode(email: String): Boolean {
        try {
            val verificationCode = generateVerificationCode()
            // emailService.send(email, verificationCode) 등 발송 로직
        } catch (e: Exception) {
            // 발송 실패 시 ErrorCode 활용
            throw BusinessException(ErrorCode.EMAIL_VERIFICATION_SEND_FAILED)
        }
    }

    private fun generateVerificationCode(): String = (100000..999999).random().toString()

    // 6. 이메일 인증번호 인증하기
    fun verifyEmailVerificationCode(email: String, code: String): Boolean {
        // DB나 캐시에서 email에 해당하는 인증번호 조회
        val storedCode = "123456" // 예시로 고정된 코드, 실제로는 DB나 캐시에서 조회해야 함 
        if (storedCode == code) {
            // 인증 성공 시 필요한 추가 로직 (예: 인증 상태 업데이트)
            return true
        } else {
            // 인증 실패 시 ErrorCode 활용
            throw BusinessException(ErrorCode.EMAIL_VERIFICATION_FAILED)
        }
    }
}