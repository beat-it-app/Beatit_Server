package com.beat_it.post.controller

import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.post.dto.NoticeCreateRequest
import com.beat_it.post.dto.NoticeDetailResponse
import com.beat_it.post.dto.NoticeListResponse
import com.beat_it.post.service.NoticeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "5. POST API", description = "공지 및 투표 관련 로직")
@RestController
@RequestMapping("/posts/notices")
class NoticeController (
    private val noticeService: NoticeService,
    private val userRepository: UserRepository
){
    @Operation(summary = "공지사항 목록 불러오기")
    @GetMapping
    fun getNoticeList(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "LATEST") sort: String
    ): ResponseEntity<BasicResponse<NoticeListResponse?>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val teamId = user.currentTeamId ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)

        val responseData = noticeService.getNoticeList(userId, teamId, keyword, sort)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "공지사항 목록을 성공적으로 불러왔습니다."))
    }

    @Operation(summary = "공지 작성하기")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createNotice(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "공지 제목", example = "[아주 중요] 합주실 사용 공지")
        @RequestParam title: String,
        @Parameter(description = "공지 본문",
                example = "((합주실 깨끗.하게 쓰세요!))")
        @RequestParam content: String,
        @RequestPart(value = "images", required = false) images: List<MultipartFile>?
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val teamId = user.currentTeamId ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)

        if (title.isBlank() || content.isBlank()) {
            throw BusinessException(ErrorCode.TITLE_CONTENT_REQUIRED)
        }

        val dto = NoticeCreateRequest(title = title, content = content)
        noticeService.createNotice(userId, teamId, dto, images)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(HttpStatus.CREATED, "공지를 성공적으로 작성했습니다."))
    }

    @Operation(summary = "공지 상세 보기")
    @GetMapping("/{noticeId}")
    fun getNotice(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noticeId: Long
    ): ResponseEntity<BasicResponse<NoticeDetailResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val response = noticeService.getNotice(userId, noticeId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "공지사항 상세 정보를 성공적으로 불러왔습니다."))
    }

    // 공지 수정하기


    // 공지 삭제하기


    // 투표 로직


    // 좋아요
    // 좋아요 있으면 싫어요 불가능하도록 -> 반대도 마찬가지

    // 싫어요
}