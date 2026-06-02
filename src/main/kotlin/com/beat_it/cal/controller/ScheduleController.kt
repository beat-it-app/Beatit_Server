package com.beat_it.cal.controller

import com.beat_it.cal.dto.CalendarSchedulesResponse
import com.beat_it.cal.dto.DateSchedulesResponse
import com.beat_it.cal.dto.ScheduleCreateRequest
import com.beat_it.cal.dto.ScheduleCreateResponse
import com.beat_it.cal.dto.ScheduleDetailResponse
import com.beat_it.cal.dto.ScheduleUpdateRequest
import com.beat_it.cal.service.ScheduleService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "4. CALENDAR API", description = "일정 관련 로직")
@RestController
@RequestMapping("/calendar")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @PostMapping
    fun createSchedule(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: ScheduleCreateRequest
    ): ResponseEntity<BasicResponse<ScheduleCreateResponse>> {

        val currentUserId = userDetails?.username?.toLong()?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val responseData = scheduleService.createSchedule(currentUserId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(responseData, HttpStatus.CREATED, "일정 생성에 성공했습니다."))
    }

    @PatchMapping("/{scheduleId}")
    fun updateSchedule(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable scheduleId: Long,
        @RequestBody request: ScheduleUpdateRequest
    ): ResponseEntity<BasicResponse<ScheduleCreateResponse>> {
        val currentUserId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val responseData = scheduleService.updateSchedule(scheduleId, currentUserId, request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "일정이 수정되었습니다."))
    }

    @DeleteMapping("/{scheduleId}")
    fun deleteSchedule(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable scheduleId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        val currentUserId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        scheduleService.deleteSchedule(scheduleId, currentUserId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "일정이 성공적으로 삭제되었습니다.")
        )
    }

    @GetMapping("/{scheduleId}")
    fun getScheduleDetail(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable scheduleId: Long
    ): ResponseEntity<BasicResponse<ScheduleDetailResponse>> {
        val currentUserId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val responseData = scheduleService.getScheduleDetail(scheduleId, currentUserId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "일정 상세 조회에 성공했습니다.")
            )
    }

    @GetMapping("/month")
    fun getCalendarSchedules(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<BasicResponse<CalendarSchedulesResponse>> {
        val currentUserId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val responseData = scheduleService.getCalendarSchedules(currentUserId, year, month)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "공유 캘린더 범위 조회에 성공했습니다."))
    }

    @GetMapping("/date")
    fun getDateSchedules(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam date: Int
    ): ResponseEntity<BasicResponse<DateSchedulesResponse>> {
        val currentUserId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
        val responseData = scheduleService.getDateSchedules(currentUserId, year, month, date)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "선택 날짜 일정 조회에 성공했습니다."))
    }

}