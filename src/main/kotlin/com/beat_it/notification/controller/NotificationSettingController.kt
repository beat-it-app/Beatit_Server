package com.beat_it.notification.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.notification.dto.NotificationSettingsResponse
import com.beat_it.notification.dto.NotificationSettingsUpdateRequest
import com.beat_it.notification.service.NotificationSettingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "9-3. NOTIFICATION SETTING API", description = "알림 설정 관련 API")
@RestController
@RequestMapping("/notifications/settings")
class NotificationSettingController(
    private val notificationSettingService: NotificationSettingService
) {

    @Operation(summary = "알림 설정 조회")
    @GetMapping
    fun getSettings(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<NotificationSettingsResponse>> {
        val userId = extractUserId(userDetails)
        val response = notificationSettingService.getSettings(userId)

        return ResponseEntity.ok(
            BasicResponse.success(response, HttpStatus.OK, "알림 설정을 성공적으로 조회했습니다.")
        )
    }

    @Operation(summary = "알림 설정 변경")
    @PatchMapping
    fun updateSettings(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: NotificationSettingsUpdateRequest
    ): ResponseEntity<BasicResponse<NotificationSettingsResponse>> {
        val userId = extractUserId(userDetails)
        val response = notificationSettingService.updateSettings(userId, request)

        return ResponseEntity.ok(
            BasicResponse.success(response, HttpStatus.OK, "알림 설정이 성공적으로 변경되었습니다.")
        )
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        return userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    }
}
