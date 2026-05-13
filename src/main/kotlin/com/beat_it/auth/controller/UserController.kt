package com.beat_it.auth.controller

import com.beat_it.auth.dto.SignUpDtoRequest
import com.beat_it.auth.service.UserService
import com.beat_it.global.response.BasicResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class UserController (
    private val userService : UserService,
){
    // 1. 회원가입
    @PostMapping("/signup")
    fun signUp(signUpDtoRequest : SignUpDtoRequest) : BasicResponse<String> {
        val data = userService.signUp(signUpDtoRequest)

        return BasicResponse.success(data, "회원가입이 성공적으로 처리되었습니다.")
    }

    // 2. 로그인
    @PostMapping("/login")
    fun login(loginDtoRequest : LoginDtoRequest) : BasicResponse<String> {
        val data = userService.login(loginDtoRequest)

        return BasicResponse.success(data, "로그인이 성공적으로 처리되었습니다.")
    }
    
    // 4. 아이디 중복 확인
    @GetMapping("/check-identifier")
    fun checkDuplicateIdentifier(@RequestParam identifier: String): BasicResponse<Boolean> {
        val data = userService.checkDuplicateIdentifier(identifier)
        return BasicResponse.success(data, if (data) "이미 사용 중인 아이디입니다." else "사용 가능한 아이디입니다.")
    }

    // 5. 이메일 인증번호 발송
    @PostMapping("/email-verification/send")
    fun sendEmailVerificationCode(@RequestParam email: String): BasicResponse<String> {
        val data = userService.sendEmailVerificationCode(email)
        return BasicResponse.success(data, "이메일 인증번호 발송이 성공적으로 처리되었습니다.")
    }

    // 6. 이메일 인증번호 인증하기
    @PostMapping("/email-verification/verify")
    fun verifyEmailVerificationCode(@RequestParam email: String, @RequestParam code: String): BasicResponse<Boolean> {
        val data = userService.verifyEmailVerificationCode(email, code)
        return BasicResponse.success(data, if (data) "이메일 인증이 성공적으로 처리되었습니다." else "잘못된 인증번호 입니다.")
    }
}