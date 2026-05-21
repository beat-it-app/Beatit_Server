package com.beat_it.auth.controller

import com.beat_it.auth.dto.LoginRequest
import com.beat_it.auth.dto.LoginResponse
import com.beat_it.auth.dto.SignUpRequest
import com.beat_it.auth.dto.SignUpResponse
import com.beat_it.auth.service.AuthService
import com.beat_it.global.response.BasicResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@SecurityRequirements()
class AuthController (
    private val userService : AuthService,
){
    @PostMapping("/signup")
    fun signUp(@RequestBody signUpRequest : SignUpRequest) : ResponseEntity<BasicResponse<SignUpResponse>> {
        val data = userService.signUp(signUpRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(data, HttpStatus.CREATED, "회원가입이 성공적으로 처리되었습니다."))
    }

    // 2. 로그인
    @PostMapping("/login")
    fun login(@RequestBody loginRequest : LoginRequest) : ResponseEntity<BasicResponse<LoginResponse>> {
        val (accessToken, data) = userService.login(loginRequest)

        return ResponseEntity
            .status(HttpStatus.OK)
            .header("Authorization", "Bearer $accessToken")
            .body(BasicResponse.success(data, HttpStatus.OK, "로그인이 성공적으로 처리되었습니다."))
    }

    // 4. 아이디 중복 확인
    @GetMapping("/check-identifier")
    fun checkDuplicateIdentifier(@RequestParam identifier: String): ResponseEntity<BasicResponse<Nothing>> {
        userService.checkDuplicateIdentifier(identifier)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "사용 가능한 아이디입니다."))
    }

    // 5. 이메일 인증번호 발송
    @PostMapping("/email-verification/send")
    fun sendEmailVerificationCode(@RequestParam email: String): ResponseEntity<BasicResponse<Nothing>> {
        userService.sendEmailVerificationCode(email)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "이메일 인증번호 발송이 성공적으로 처리되었습니다."))
    }

    // 6. 이메일 인증번호 인증하기
    @PostMapping("/email-verification/verify")
    fun verifyEmailVerificationCode(@RequestParam email: String, @RequestParam code: String): ResponseEntity<BasicResponse<Nothing>> {
        userService.verifyEmailVerificationCode(email, code)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "이메일 인증이 성공적으로 처리되었습니다."))
    }

    @GetMapping
    fun getUser(@AuthenticationPrincipal userDetails: UserDetails) : String {
        val publicId = userDetails.username
        // TODO : 여기서 사용자 아이디, 팀 아이디 받아야 함
        // TODO : 이 기능은 다른 모듈에서도 사용할 수 있어야 함
        return publicId
    }
}