package com.beat_it.cal.controller

import com.beat_it.cal.dto.CalendarSchedulesResponse
import com.beat_it.cal.dto.DateSchedulesResponse
import com.beat_it.cal.dto.ScheduleCreateRequest
import com.beat_it.cal.dto.ScheduleCreateResponse
import com.beat_it.cal.dto.ScheduleDetailResponse
import com.beat_it.cal.dto.ScheduleUpdateRequest
import com.beat_it.cal.service.ScheduleService
import com.beat_it.global.response.BasicResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/calendar")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @PostMapping
    fun createSchedule(
        @RequestHeader("X-USER-ID") userId: Long,
        @RequestBody request: ScheduleCreateRequest
    ): ResponseEntity<BasicResponse<ScheduleCreateResponse>> {

        val responseData = scheduleService.createSchedule(userId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(responseData, HttpStatus.CREATED, "일정 생성에 성공했습니다."))
    }

    @PatchMapping("/{scheduleId}")
    fun updateSchedule(
        @RequestHeader("X-USER-ID") userId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody request: ScheduleUpdateRequest
    ): ResponseEntity<BasicResponse<ScheduleCreateResponse>> {
        val responseData = scheduleService.updateSchedule(scheduleId, userId, request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "일정이 수정되었습니다."))
    }

    @DeleteMapping("/{scheduleId}")
    fun deleteSchedule(
        @RequestHeader("X-USER-ID") userId: Long,
        @PathVariable scheduleId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        scheduleService.deleteSchedule(scheduleId, userId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "일정이 성공적으로 삭제되었습니다.")
        )
    }

    @GetMapping("/{scheduleId}")
    fun getScheduleDetail(
        @PathVariable scheduleId: Long
    ): ResponseEntity<BasicResponse<ScheduleDetailResponse>> {
        val responseData = scheduleService.getScheduleDetail(scheduleId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "일정 상세 조회에 성공했습니다.")
            )
    }

    @GetMapping("/month")
    fun getCalendarSchedules(
        @RequestHeader("X-USER-ID") userId: Long,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<BasicResponse<CalendarSchedulesResponse>> {
        val responseData = scheduleService.getCalendarSchedules(userId, year, month)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "공유 캘린더 범위 조회에 성공했습니다."))
    }

    @GetMapping("/date")
    fun getDateSchedules(
        @RequestHeader("X-USER-ID") userId: Long,
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam date: Int
    ): ResponseEntity<BasicResponse<DateSchedulesResponse>> { // 반환 DTO 타입을 명확히 컴파일 시점에 지정 가능!
        val responseData = scheduleService.getDateSchedules(userId, year, month, date)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "선택 날짜 일정 조회에 성공했습니다."))
    }

}