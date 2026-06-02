package com.beat_it.auth.controller

import com.beat_it.auth.dto.LoginRequest
import com.beat_it.auth.dto.LoginResponse
import com.beat_it.auth.dto.SignUpRequest
import com.beat_it.auth.dto.SignUpResponse
import com.beat_it.auth.service.AuthService
import com.beat_it.global.response.BasicResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "1. AUTH API", description = "회원가입 및 로그인 로직")
@RestController
@RequestMapping("/auth")
@SecurityRequirements()
class AuthController (
    private val authService : AuthService,
){
    @PostMapping("/signup")
    fun signUp(@RequestBody signUpRequest : SignUpRequest) : ResponseEntity<BasicResponse<SignUpResponse>> {
        val data = authService.signUp(signUpRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(data, HttpStatus.CREATED, "회원가입이 성공적으로 처리되었습니다."))
    }

    // 2. 로그인
    @PostMapping("/login")
    fun login(@RequestBody loginRequest : LoginRequest) : ResponseEntity<BasicResponse<LoginResponse>> {
        val (accessToken, data) = authService.login(loginRequest)

        return ResponseEntity
            .status(HttpStatus.OK)
            .header("Authorization", "Bearer $accessToken")
            .body(BasicResponse.success(data, HttpStatus.OK, "로그인이 성공적으로 처리되었습니다."))
    }

    // 4. 아이디 중복 확인
    @GetMapping("/check-identifier")
    fun checkDuplicateIdentifier(@RequestParam identifier: String): ResponseEntity<BasicResponse<Nothing>> {
        authService.checkDuplicateIdentifier(identifier)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "사용 가능한 아이디입니다."))
    }

    // 5. 이메일 인증번호 발송
    @PostMapping("/email-verification/send")
    fun sendEmailVerificationCode(@RequestParam email: String): ResponseEntity<BasicResponse<Nothing>> {
        authService.sendEmailVerificationCode(email)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "이메일 인증번호 발송이 성공적으로 처리되었습니다."))
    }

    // 6. 이메일 인증번호 인증하기
    @PostMapping("/email-verification/verify")
    fun verifyEmailVerificationCode(@RequestParam email: String, @RequestParam code: String): ResponseEntity<BasicResponse<Nothing>> {
        authService.verifyEmailVerificationCode(email, code)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "이메일 인증이 성공적으로 처리되었습니다."))
    }
}