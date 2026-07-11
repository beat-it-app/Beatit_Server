package com.beat_it.team.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.team.dto.ArchiveCreateRequest
import com.beat_it.team.dto.ArchiveCreateResponse
import com.beat_it.team.dto.ArchiveDetailResponse
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
import tools.jackson.databind.ObjectMapper

@Tag(name = "3-2. TEAM ARCHIVE API", description = "팀 연습실 수정 및 생성 관리 로직")
@RestController
@RequestMapping("/teams/archives")
class ArchiveController(
    private val archiveService: ArchiveService,
    private val objectMapper: ObjectMapper,
) {
    @Operation(summary = "연습실 생성하기")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createArchive(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam("request") requestJson: String,
        @RequestPart(value = "archiveImage", required = false) archiveImage: MultipartFile?
    ): ResponseEntity<BasicResponse<ArchiveCreateResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val request = objectMapper.readValue(
            requestJson,
            ArchiveCreateRequest::class.java,
        )

        val responseData = archiveService.createArchive(
            userId = userId,
            request = request,
            archiveImage = archiveImage,
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
        @RequestParam(value = "request", required = false) requestJson: String?,
        @RequestPart(value = "archiveImage", required = false) archiveImage: MultipartFile?,
    ): ResponseEntity<BasicResponse<ArchiveUpdateResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val request = requestJson
            ?.takeIf { it.isNotBlank() }
            ?.let {
                objectMapper.readValue(
                    it,
                    ArchiveUpdateRequest::class.java,
                )
            }
            ?: ArchiveUpdateRequest()

        val responseData = archiveService.updateArchive(
            userId = userId,
            archiveId = archiveId,
            request = request,
            archiveImage = archiveImage,
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


//    @Operation(summary = "연습실 목록 확인하기")
//    @GetMapping
//    fun getMyTeamArchives(
//        @AuthenticationPrincipal userDetails: UserDetails?,
//    ): ResponseEntity<BasicResponse<UserTeamListResponse>> {
//        val userId = userDetails?.username?.toLong()
//            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

//        val responseData = archiveService.getTeamArchives(userId)

//        return ResponseEntity.ok(
//            BasicResponse.success(responseData, HttpStatus.OK, "나의 팀 리스트 조회에 성공했습니다.")
//        )
//    }
}

