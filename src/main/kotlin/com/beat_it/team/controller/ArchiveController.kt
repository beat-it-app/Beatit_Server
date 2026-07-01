package com.beat_it.team.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.TeamDetailUpdateRequest
import com.beat_it.team.dto.TeamDetailUpdateResponse
import com.beat_it.global.response.BasicResponse
import com.beat_it.team.dto.ArchiveCreateRequest
import com.beat_it.team.dto.ArchiveCreateResponse
import com.beat_it.team.dto.ArchiveDetailResponse
import com.beat_it.team.dto.ArchiveUpdateRequest
import com.beat_it.team.dto.ArchiveUpdateResponse
import com.beat_it.team.dto.UserTeamListResponse
import com.beat_it.team.service.ArchiveService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails

@Tag(name = "3-2. TEAM ARCHIVE API", description = "팀 연습실 수정 및 생성 관리 로직")
@RestController
@RequestMapping("/teams/archives")
class ArchiveController(
    private val archiveService: ArchiveService,
) {
    @Operation(summary = "연습실 생성하기")
    @PostMapping
    fun createArchive(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: ArchiveCreateRequest
    ): ResponseEntity<BasicResponse<ArchiveCreateResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = archiveService.createArchive(userId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(responseData, HttpStatus.CREATED, "연습실 생성에 성공했습니다."))
    }

    @Operation(summary = "연습실 수정하기")
    @PatchMapping("/{archiveId}")
    fun updateArchive(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable("archiveId") archiveId: Long,
        @RequestBody request: ArchiveUpdateRequest,
    ): ResponseEntity<BasicResponse<ArchiveUpdateResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = archiveService.updateArchive(userId, archiveId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "연습실 상세 내용이 수정되었습니다."))
    }

    @Operation(summary = "연습실 삭제하기")
    @DeleteMapping("/{archiveId}")
    fun deleteArchive(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable("archiveId") archiveId: Long
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
        @PathVariable("archiveId") archiveId: Long
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

