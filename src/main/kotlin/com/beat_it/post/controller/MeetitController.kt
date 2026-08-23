package com.beat_it.post.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.post.dto.meetit.MeetitCreateRequest
import com.beat_it.post.dto.meetit.MeetitDetailResponse
import com.beat_it.post.dto.meetit.MeetitListResponse
import com.beat_it.post.dto.meetit.MeetitSubmissionRequest
import com.beat_it.post.service.MeetitService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "4-3. (POST) MEETIT API", description = "밋잇 관련 로직")
@RestController
@RequestMapping("/posts/meetit")
class MeetitController(
    private val meetitService: MeetitService
) {

    @Operation(summary = "밋잇 목록 불러오기")
    @GetMapping
    fun getMeetitList(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<BasicResponse<MeetitListResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val response = meetitService.getMeetitList(userId, page, size)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "밋잇 목록을 성공적으로 불러왔습니다."))
    }

    @Operation(summary = "밋잇 생성하기")
    @PostMapping
    fun createMeetit(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: MeetitCreateRequest
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        meetitService.createMeetit(userId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(HttpStatus.CREATED, "밋잇을 성공적으로 생성했습니다."))
    }

    @Operation(summary = "밋잇 상세 보기")
    @GetMapping("/{meetitId}")
    fun getMeetitDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable meetitId: Long,
        @RequestParam(defaultValue = "ALL") filter: String,
        @RequestParam(required = false) userIds: List<Long>?
    ): ResponseEntity<BasicResponse<MeetitDetailResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val response = meetitService.getMeetitDetail(userId, meetitId, filter, userIds)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "밋잇 상세 정보를 성공적으로 불러왔습니다."))
    }

    @Operation(summary = "밋잇 응답하기")
    @PostMapping("/{meetitId}/responses")
    fun respondMeetit(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable meetitId: Long,
        @RequestBody request: MeetitSubmissionRequest
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        meetitService.respondMeetit(userId, meetitId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "성공적으로 응답하였습니다."))
    }
}