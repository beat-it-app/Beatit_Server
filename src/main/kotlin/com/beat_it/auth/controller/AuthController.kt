package com.beat_it.auth.controller

import com.beat_it.auth.dto.LoginRequest
import com.beat_it.auth.dto.LoginResponse
import com.beat_it.auth.dto.SignUpRequest
import com.beat_it.auth.dto.SignUpResponse
import com.beat_it.auth.dto.SocialLoginRequest
import com.beat_it.auth.dto.ReissueResponse
import com.beat_it.auth.dto.FindIdentifierResponse
import com.beat_it.auth.dto.ResetPasswordRequest
import com.beat_it.auth.dto.ResetPasswordResponse
import com.beat_it.auth.service.AuthService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest

@Tag(name = "1-1. AUTH API", description = "회원가입 및 로그인 로직")
@RestController
@RequestMapping("/auth")
@SecurityRequirements()
class AuthController (
    private val authService : AuthService,
){
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    fun signUp(@RequestBody signUpRequest : SignUpRequest) : ResponseEntity<BasicResponse<SignUpResponse>> {
        val data = authService.signUp(signUpRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(data, HttpStatus.CREATED, "회원가입이 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(@RequestBody loginRequest : LoginRequest) : ResponseEntity<BasicResponse<LoginResponse>> {
        val (accessToken, refreshToken, data) = authService.login(loginRequest)

        return ResponseEntity
            .status(HttpStatus.OK)
            .header("Authorization", "Bearer $accessToken")
            .header("Refresh-Token", refreshToken)
            .body(BasicResponse.success(data, HttpStatus.OK, "로그인이 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "아이디 중복확인")
    @GetMapping("/check-identifier")
    fun checkDuplicateIdentifier(@RequestParam identifier: String): ResponseEntity<BasicResponse<Nothing>> {
        authService.checkDuplicateIdentifier(identifier)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "사용 가능한 아이디입니다."))
    }

    @Operation(summary = "구글 로그인")
    @PostMapping("/google")
    fun googleLogin(@RequestBody socialLoginRequest: SocialLoginRequest): ResponseEntity<BasicResponse<LoginResponse>> {
        val (accessToken, refreshToken, data) = authService.googleLogin(socialLoginRequest)

        return ResponseEntity
            .status(HttpStatus.OK)
            .header("Authorization", "Bearer $accessToken")
            .header("Refresh-Token", refreshToken)
            .body(BasicResponse.success(data, HttpStatus.OK, "구글 로그인이 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "이메일 인증번호 발송")
    @PostMapping("/email-verification/send")
    fun sendEmailVerificationCode(@RequestParam email: String): ResponseEntity<BasicResponse<Nothing>> {
        authService.sendEmailVerificationCode(email)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "이메일 인증번호 발송이 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "이메일 인증번호 인증하기")
    @PostMapping("/email-verification/verify")
    fun verifyEmailVerificationCode(@RequestParam email: String, @RequestParam code: String): ResponseEntity<BasicResponse<Nothing>> {
        authService.verifyEmailVerificationCode(email, code)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "이메일 인증이 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    fun reissue(
        request: HttpServletRequest,
        @RequestHeader(value = "Refresh-Token", required = false) headerRefreshToken: String?
    ): ResponseEntity<BasicResponse<ReissueResponse>> {
        val refreshToken = headerRefreshToken
            ?: request.cookies?.find { it.name == "refresh_token" }?.value
            ?: throw BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        val (newAccessToken, newRefreshToken) = authService.reissue(refreshToken)

        val responseBody = ReissueResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .header("Authorization", "Bearer $newAccessToken")
            .header("Refresh-Token", newRefreshToken)
            .body(BasicResponse.success(responseBody, HttpStatus.OK, "토큰이 성공적으로 재발급되었습니다."))
    }

    @Operation(summary = "아이디 찾기 인증번호 발송")
    @PostMapping("/find-identifier/send")
    fun sendFindIdentifierCode(@RequestParam email: String): ResponseEntity<BasicResponse<Nothing>> {
        authService.sendFindIdentifierCode(email)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "아이디 찾기 인증번호 발송이 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "아이디 찾기 인증번호 인증")
    @PostMapping("/find-identifier/verify")
    fun verifyFindIdentifierCode(
        @RequestParam email: String, 
        @RequestParam code: String
    ): ResponseEntity<BasicResponse<FindIdentifierResponse>> {
        val response = authService.verifyFindIdentifierCode(email, code)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "아이디 조회가 성공적으로 완료되었습니다."))
    }

    @Operation(summary = "비밀번호 재설정 인증번호 발송")
    @PostMapping("/reset-password/send")
    fun sendResetPasswordCode(
        @RequestParam identifier: String, 
        @RequestParam email: String
    ): ResponseEntity<BasicResponse<Nothing>> {
        authService.sendResetPasswordCode(identifier, email)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "비밀번호 재설정 인증번호 발송이 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "비밀번호 재설정 인증번호 인증")
    @PostMapping("/reset-password/verify")
    fun verifyResetPasswordCode(
        @RequestParam email: String, 
        @RequestParam code: String
    ): ResponseEntity<BasicResponse<Nothing>> {
        authService.verifyResetPasswordCode(email, code)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "비밀번호 재설정 인증이 완료되었습니다."))
    }

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<BasicResponse<ResetPasswordResponse>> {
        val response = authService.resetPassword(request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "비밀번호 재설정이 성공적으로 처리되었습니다."))
    }
}