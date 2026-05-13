package com.beat_it.cal.controller

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

        // 억지로 형식을 만들지 않고, 정의된 BasicResponse 규격 그대로 반환합니다.
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(responseData, "일정 생성에 성공했습니다."))
    }

    @PatchMapping("/{scheduleId}")
    fun updateSchedule(
        @RequestHeader("X-USER-ID") userId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody request: ScheduleUpdateRequest
    ): ResponseEntity<BasicResponse<ScheduleCreateResponse>> {
        val responseData = scheduleService.updateSchedule(scheduleId, userId, request)
        return ResponseEntity.ok(BasicResponse.success(responseData, "일정이 수정되었습니다."))
    }

    @DeleteMapping("/{scheduleId}")
    fun deleteSchedule(
        @RequestHeader("X-USER-ID") userId: Long,
        @PathVariable scheduleId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        scheduleService.deleteSchedule(scheduleId, userId)
        return ResponseEntity.ok(
            BasicResponse.success("일정이 성공적으로 삭제되었습니다.")
        )
    }

    @GetMapping("/{scheduleId}")
    fun getScheduleDetail(
        @PathVariable scheduleId: Long
    ): ResponseEntity<BasicResponse<ScheduleDetailResponse>> {
        val responseData = scheduleService.getScheduleDetail(scheduleId)
        return ResponseEntity.ok(
            BasicResponse.success(responseData, "일정 상세 조회에 성공했습니다.")
        )
    }
}