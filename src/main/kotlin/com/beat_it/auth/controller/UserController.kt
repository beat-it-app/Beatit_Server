package com.beat_it.auth.controller

//import com.beat_it.auth.dto.LoginRequest
import com.beat_it.auth.dto.SignUpRequest
import com.beat_it.auth.dto.SignUpResponse
import com.beat_it.auth.service.UserService
import com.beat_it.global.response.BasicResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class UserController (
    private val userService : UserService,
){
    // 1. 회원가입
    @PostMapping("/signup")
    fun signUp(@RequestBody signUpRequest : SignUpRequest) : ResponseEntity<BasicResponse<SignUpResponse>> {
        val data = userService.signUp(signUpRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(data, "회원가입이 성공적으로 처리되었습니다."))
    }

    // 2. 로그인
//    @PostMapping("/login")
//    fun login(loginRequest : LoginRequest) : ResponseEntity<BasicResponse<LoginResponse>> {
//        val data = userService.login(loginRequest)
//
//        return ResponseEntity
//            .status(HttpStatus.OK)
//            .body(BasicResponse.success(data, "로그인이 성공적으로 처리되었습니다."))
//    }
    
    // 4. 아이디 중복 확인
    @GetMapping("/check-identifier")
    fun checkDuplicateIdentifier(@RequestParam identifier: String): ResponseEntity<BasicResponse<Nothing>> {
        userService.checkDuplicateIdentifier(identifier)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success("사용 가능한 아이디입니다."))
    }

    // 5. 이메일 인증번호 발송
    @PostMapping("/email-verification/send")
    fun sendEmailVerificationCode(@RequestParam email: String): ResponseEntity<BasicResponse<Nothing>> {
        userService.sendEmailVerificationCode(email)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success("이메일 인증번호 발송이 성공적으로 처리되었습니다."))
    }

    // 6. 이메일 인증번호 인증하기
    @PostMapping("/email-verification/verify")
    fun verifyEmailVerificationCode(@RequestParam email: String, @RequestParam code: String): ResponseEntity<BasicResponse<Nothing>> {
        userService.verifyEmailVerificationCode(email, code)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success("이메일 인증이 성공적으로 처리되었습니다."))
    }
}