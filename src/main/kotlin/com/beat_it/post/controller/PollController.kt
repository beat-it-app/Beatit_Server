package com.beat_it.post.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.post.dto.*
import com.beat_it.post.dto.poll.PollDetailResponse
import com.beat_it.post.dto.poll.PollListResponse
import com.beat_it.post.dto.poll.PollRequest
import com.beat_it.post.dto.poll.VoteRequest
import com.beat_it.post.service.PollService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "4-2. (POST) POLL API", description = "투표 관련 로직")
@RestController
@RequestMapping("/posts/poll")
class PollController (
    private val pollService: PollService
){
    @Operation(summary = "투표 목록 불러오기")
    @GetMapping
    fun getPollList(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<BasicResponse<PollListResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val response = pollService.getPollList(userId, keyword, page, size)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "투표 목록을 성공적으로 불러왔습니다."))
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

    @Operation(summary = "투표 상세 보기")
    @GetMapping("/{pollId}")
    fun getPoll(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable pollId: Long
    ): ResponseEntity<BasicResponse<PollDetailResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val response = pollService.getPoll(userId, pollId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "투표 상세 정보를 성공적으로 불러왔습니다."))
    }

    @Operation(summary = "투표 하기")
    @PostMapping("/{pollId}/votes")
    fun votePoll(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable pollId: Long,
        @RequestBody request: VoteRequest
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        pollService.votePoll(userId, pollId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "성공적으로 투표하였습니다."))
    }

    @Operation(summary = "투표 삭제하기 - 작성자만 가능")
    @DeleteMapping("/{pollId}")
    fun deletePoll(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable pollId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        pollService.deletePoll(userId, pollId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "투표가 성공적으로 삭제되었습니다."))
    }

    @Operation(summary = "투표 댓글 작성하기")
    @PostMapping("/{pollId}/comments")
    fun createComment(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable pollId: Long,
        @RequestBody request: CommentRequest
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        pollService.createComment(userId, pollId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "댓글이 성공적으로 등록되었습니다."))
    }

    @Operation(summary = "투표 댓글 삭제하기 - 댓글 작성자 혹은 투표 작성자만 가능")
    @DeleteMapping("/{pollId}/comments/{commentId}")
    fun deleteComment(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable pollId: Long,
        @PathVariable commentId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        pollService.deleteComment(userId, pollId, commentId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "댓글이 성공적으로 삭제되었습니다."))
    }
}