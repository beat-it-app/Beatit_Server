package com.beat_it.notification.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.notification.dto.PushTokenRegisterRequest
import com.beat_it.notification.dto.PushTokenResponse
import com.beat_it.notification.service.PushTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "8-0. PUSH TOKEN API", description = "기기 푸시 토큰 관리")
@RestController
@RequestMapping("/push-tokens")
class PushTokenController(
    private val pushTokenService: PushTokenService
) {

    @Operation(summary = "푸시 토큰 등록 및 갱신", description = "앱 실행 시 발급받은 디바이스 푸시 토큰을 서버에 등록하거나 갱신합니다.")
    @PostMapping
    fun registerToken(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: PushTokenRegisterRequest
    ): ResponseEntity<BasicResponse<PushTokenResponse>> {
        val userId = extractUserId(userDetails)
        val response = pushTokenService.registerOrUpdateToken(userId, request)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            BasicResponse.success(response, HttpStatus.CREATED, "푸시 토큰이 성공적으로 등록되었습니다.")
        )
    }

    @Operation(summary = "푸시 토큰 비활성화", description = "로그아웃 또는 알림 끄기 시 해당 기기의 푸시 토큰을 비활성화합니다.")
    @DeleteMapping("/{deviceId}")
    fun deactivateToken(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "디바이스 고유 식별자 ID")
        @PathVariable deviceId: String
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)
        pushTokenService.deactivateToken(userId, deviceId)

        return ResponseEntity.ok(
            BasicResponse.success(HttpStatus.OK, "푸시 토큰이 비활성화되었습니다.")
        )
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        return userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    }
}
