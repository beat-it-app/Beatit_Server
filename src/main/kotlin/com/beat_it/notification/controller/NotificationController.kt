package com.beat_it.notification.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.notification.dto.*
import com.beat_it.notification.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "9-1. NOTIFICATION API", description = "팀별 알림 관련 로직")
@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @Operation(summary = "현재 팀의 알림 목록 조회")
    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<BasicResponse<NotificationPageResponse>> {
        val userId = extractUserId(userDetails)
        val response = notificationService.getNotifications(userId, page, size)

        return ResponseEntity.ok(
            BasicResponse.success(response, HttpStatus.OK, "알림 목록을 성공적으로 조회했습니다.")
        )
    }

    @Operation(summary = "인앱 알림 클릭 및 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    fun markAsRead(@AuthenticationPrincipal userDetails: UserDetails, @PathVariable notificationId: Long
    ): ResponseEntity<BasicResponse<NotificationClickResponse>> {
        val userId = extractUserId(userDetails)
        val response = notificationService.markAsRead(userId, notificationId)

        return ResponseEntity.ok(
            BasicResponse.success(response, HttpStatus.OK, "알림이 읽음 처리되었으며 direct 이동 정보를 반환합니다.")
        )
    }

    @Operation(summary = "푸시 알림 탭(클릭) 시 direct 이동 및 조건부 팀 전환")
    @PostMapping("/{notificationId}/click")
    fun clickNotification(@AuthenticationPrincipal userDetails: UserDetails, @PathVariable notificationId: Long
    ): ResponseEntity<BasicResponse<NotificationClickResponse>> {
        val userId = extractUserId(userDetails)
        val response = notificationService.clickNotification(userId, notificationId)

        return ResponseEntity.ok(
            BasicResponse.success(response, HttpStatus.OK, "알림이 처리되었으며 direct 이동 정보를 반환합니다.")
        )
    }

    @Operation(summary = "현재 팀의 모든 알림 읽음 처리")
    @PatchMapping("/read-all")
    fun markAllAsRead(@AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)
        notificationService.markAllAsRead(userId)

        return ResponseEntity.ok(
            BasicResponse.success(HttpStatus.OK, "모든 알림이 읽음 처리되었습니다.")
        )
    }

    @Operation(summary = "[테스트용] 카프카를 통한 샘플 알림 4개 발송 테스트")
    @PostMapping("/test/mock-data")
    fun createMockData(
        @RequestParam userId: Long,
        @RequestParam teamId: Long
    ): ResponseEntity<BasicResponse<String>> {
        val response = notificationService.createMockNotifications(userId, teamId)

        return ResponseEntity.ok(
            BasicResponse.success(response, HttpStatus.OK, "카프카를 통해 샘플 알림 4개가 성공적으로 발행되었습니다.")
        )
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        return userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    }
}
