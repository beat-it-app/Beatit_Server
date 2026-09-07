package com.beat_it.post.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.post.dto.CommentRequest
import com.beat_it.post.dto.notice.NoticeRequest
import com.beat_it.post.dto.notice.NoticeDetailResponse
import com.beat_it.post.dto.notice.NoticeListResponse
import com.beat_it.post.entity.enum.NoticeSortType
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

@Tag(name = "4-1. (POST) NOTICE API", description = "공지 관련 로직")
@RestController
@RequestMapping("/posts/notices")
class NoticeController (
    private val noticeService: NoticeService
){
    @Operation(summary = "공지사항 목록 불러오기")
    @GetMapping
    fun getNoticeList(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "LATEST") sort: NoticeSortType,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<BasicResponse<NoticeListResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = noticeService.getNoticeList(userId, keyword, sort, page, size)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "공지사항 목록을 성공적으로 불러왔습니다."))
    }

    @Operation(summary = "공지 작성하기")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createNotice(
        @AuthenticationPrincipal userDetails: UserDetails,
        // TODO : 제목이랑 본문은 parameter로 받으면 안될 것 같은데.. 어떻게 수정할 수 있는지 모르겠음! (공지 수정도 마찬가지)
        @Parameter(description = "공지 제목", example = "[아주 중요] 합주실 사용 공지")
        @RequestParam title: String,
        @Parameter(description = "공지 본문", example = "((합주실 깨끗.하게 쓰세요!))")
        @RequestParam content: String,
        @RequestPart(value = "images", required = false) images: List<MultipartFile>?
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val dto = NoticeRequest(title = title, content = content)
        noticeService.createNotice(userId, dto, images)

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

    @Operation(summary = "공지 수정하기 - 작성자만 가능")
    @PostMapping(value = ["/{noticeId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun editNotice(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noticeId: Long,
        @Parameter(description = "공지 제목", example = "[수정] 합주실 사용 공지")
        @RequestParam title: String,
        @Parameter(description = "공지 본문", example = "수정된 내용입니다.")
        @RequestParam content: String,
        @RequestPart(value = "images", required = false) images: List<MultipartFile>?
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val dto = NoticeRequest(title = title, content = content)
        noticeService.editNotice(userId, noticeId, dto, images)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "공지사항을 성공적으로 수정했습니다."))
    }

    @Operation(summary = "공지 삭제하기 - 작성자만 가능")
    @DeleteMapping("/{noticeId}")
    fun deleteNotice(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noticeId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        noticeService.deleteNotice(userId, noticeId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "공지사항이 성공적으로 삭제되었습니다."))
    }

    @Operation(summary = "공지 좋아요 토글")
    @PostMapping("/{noticeId}/like")
    fun toggleLike(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noticeId: Long
    ): ResponseEntity<BasicResponse<Boolean>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val isLiked = noticeService.toggleLike(userId, noticeId)
        val message = if (isLiked) "좋아요를 눌렀습니다." else "좋아요를 취소했습니다."

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(isLiked, HttpStatus.OK, message))
    }

    @Operation(summary = "공지 싫어요 토글")
    @PostMapping("/{noticeId}/dislike")
    fun toggleDislike(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noticeId: Long
    ): ResponseEntity<BasicResponse<Boolean>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val isDisliked = noticeService.toggleDislike(userId, noticeId)
        val message = if (isDisliked) "싫어요를 눌렀습니다." else "싫어요를 취소했습니다."

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(isDisliked, HttpStatus.OK, message))
    }

    @Operation(summary = "댓글 달기")
    @PostMapping("/{noticeId}/comments")
    fun createComment(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noticeId: Long,
        @RequestBody request: CommentRequest
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        noticeService.createComment(userId, noticeId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "댓글이 성공적으로 등록되었습니다."))
    }

    @Operation(summary = "댓글 삭제하기 - 댓글 작성자 혹은 공지 작성자만 가능")
    @DeleteMapping("/{noticeId}/comments/{commentId}")
    fun deleteComment(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable noticeId: Long,
        @PathVariable commentId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        noticeService.deleteComment(userId, noticeId, commentId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "댓글이 성공적으로 삭제되었습니다."))
    }
}
