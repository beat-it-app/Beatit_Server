package com.beat_it.team.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.post.dto.CommentRequest
import com.beat_it.team.dto.ArchiveCreateRequest
import com.beat_it.team.dto.ArchiveCreateResponse
import com.beat_it.team.dto.ArchiveDetailResponse
import com.beat_it.team.dto.ArchiveListResponse
import com.beat_it.team.dto.ArchiveRatingRequest
import com.beat_it.team.dto.ArchiveRatingResponse
import com.beat_it.team.dto.ArchiveUpdateRequest
import com.beat_it.team.dto.ArchiveUpdateResponse
import com.beat_it.team.service.ArchiveService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.multipart.MultipartFile

@Tag(name = "2-2. TEAM ARCHIVE API", description = "팀 연습실 수정 및 생성 관리 로직")
@RestController
@RequestMapping("/teams/archives")
class ArchiveController(
    private val archiveService: ArchiveService,
) {
    @Operation(summary = "연습실 생성하기")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createArchive(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("request") request: ArchiveCreateRequest,
        @RequestPart(value = "archiveImages", required = false) archiveImages: List<MultipartFile>?,
    ): ResponseEntity<BasicResponse<ArchiveCreateResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = archiveService.createArchive(
            userId = userId,
            request = request,
            archiveImages = archiveImages,
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(responseData, HttpStatus.CREATED, "연습실 생성에 성공했습니다."))
    }

    @Operation(summary = "연습실 수정하기")
    @PatchMapping(
        "/{archiveId}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun updateArchive(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable archiveId: Long,
        @RequestPart(value = "request", required = false) request: ArchiveUpdateRequest?,
        @RequestPart(value = "archiveImages", required = false) archiveImages: List<MultipartFile>?,
    ): ResponseEntity<BasicResponse<ArchiveUpdateResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = archiveService.updateArchive(
            userId = userId,
            archiveId = archiveId,
            request = request ?: ArchiveUpdateRequest(),
            archiveImages = archiveImages,
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "연습실 상세 내용이 수정되었습니다."))
    }

    @Operation(summary = "연습실 삭제하기")
    @DeleteMapping("/{archiveId}")
    fun deleteArchive(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable archiveId: Long
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        archiveService.deleteArchive(userId, archiveId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "연습실이 성공적으로 삭제되었습니다."))
    }

    @Operation(summary = "연습실 상세 확인하기")
    @GetMapping("/{archiveId}")
    fun getArchiveDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable archiveId: Long
    ): ResponseEntity<BasicResponse<ArchiveDetailResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val archiveDetail = archiveService.getArchiveDetail(userId, archiveId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(archiveDetail, HttpStatus.OK, "연습실 상세 내용 조회에 성공했습니다."))
    }

    @Operation(summary = "연습실 별점 등록 및 수정")
    @PostMapping("/{archiveId}/ratings")
    fun saveRating(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable archiveId: Long,
        @RequestBody request: ArchiveRatingRequest,
    ): ResponseEntity<BasicResponse<ArchiveRatingResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = archiveService.saveRating(userId, archiveId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "별점이 저장되었습니다."))
    }

    @Operation(summary = "연습실 댓글 작성하기")
    @PostMapping("/{archiveId}/comments")
    fun createComment(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable archiveId: Long,
        @RequestBody request: CommentRequest,
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        archiveService.createComment(userId, archiveId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "댓글이 성공적으로 등록되었습니다."))
    }

    @Operation(summary = "연습실 댓글 삭제하기 - 댓글 작성자 혹은 연습실 작성자만 가능")
    @DeleteMapping("/{archiveId}/comments/{commentId}")
    fun deleteComment(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable archiveId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        archiveService.deleteComment(userId, archiveId, commentId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "댓글이 성공적으로 삭제되었습니다."))
    }


    @Operation(summary = "연습실 목록 확인하기")
    @GetMapping
    fun getTeamArchives(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<BasicResponse<ArchiveListResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = archiveService.getTeamArchives(userId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "연습실 목록 조회에 성공했습니다."))
    }
}
