package com.beat_it.post.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.post.dto.PollListResponse
import com.beat_it.post.dto.PollRequest
import com.beat_it.post.service.PollService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "5-2. (POST) POLL API", description = "투표 관련 로직")
@RestController
@RequestMapping("/posts/poll")
class PollController (
    private val pollService: PollService
){
    @Operation(summary = "투표 목록 불러오기")
    @GetMapping
    fun getPollList(@AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<PollListResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val resopnse = pollService.getPollList(userId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(resopnse, HttpStatus.OK, "투표 목록을 성공적으로 불러왔습니다."))
    }

    @Operation(summary = "투표 생성하기")
    @PostMapping
    fun postPoll(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody dto: PollRequest
        ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        pollService.postPoll(userId, dto)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(HttpStatus.CREATED, "투표를 성공적으로 생성했습니다."))
    }
}