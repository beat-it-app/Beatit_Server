package com.beat_it.team.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.team.dto.TeamCloudFileDetailResponse
import com.beat_it.team.dto.TeamCloudFolderCreateRequest
import com.beat_it.team.dto.TeamCloudFolderUpdateRequest
import com.beat_it.team.dto.TeamCloudItemsDeleteRequest
import com.beat_it.team.dto.TeamCloudItemsMoveRequest
import com.beat_it.team.dto.TeamCloudLinkCreateRequest
import com.beat_it.team.dto.TeamCloudListResponse
import com.beat_it.team.service.TeamCloudService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/teams/clouds")
class TeamCloudController(
    private val teamCloudService: TeamCloudService
) {
    @GetMapping
    fun getTeamCloudList(
        @RequestParam(required = false) folderId: Long?,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<TeamCloudListResponse>> {
        val userId = extractUserId(userDetails)
        val response = teamCloudService.getTeamCloudList(userId, folderId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "팀 클라우드 목록을 성공적으로 조회했습니다."))
    }

    @GetMapping("/files/{itemId}")
    fun getTeamCloudFileDetail(
        @PathVariable itemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<TeamCloudFileDetailResponse>> {
        val userId = extractUserId(userDetails)
        val response = teamCloudService.getTeamCloudFileDetail(userId, itemId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "팀 클라우드 파일 상세 조회를 성공했습니다."))
    }

    @PostMapping("/folders")
    fun createFolder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: TeamCloudFolderCreateRequest
    ): ResponseEntity<BasicResponse<Long>> {
        val userId = extractUserId(userDetails)
        val folderId = teamCloudService.createFolder(userId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(folderId, HttpStatus.CREATED, "폴더가 성공적으로 생성되었습니다."))
    }

    @PatchMapping("/folders/{folderId}")
    fun updateFolder(
        @PathVariable folderId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: TeamCloudFolderUpdateRequest
    ): ResponseEntity<BasicResponse<Nothing?>> {
        val userId = extractUserId(userDetails)
        teamCloudService.updateFolder(userId, folderId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(null, HttpStatus.OK, "폴더 이름이 성공적으로 변경되었습니다."))
    }

    @PatchMapping("/items/move")
    fun moveItems(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: TeamCloudItemsMoveRequest
    ): ResponseEntity<BasicResponse<Nothing?>> {
        val userId = extractUserId(userDetails)
        teamCloudService.moveItems(userId, request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(null, HttpStatus.OK, "아이템이 성공적으로 이동되었습니다."))
    }

    @PostMapping(value = ["/files"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestParam(required = false) folderId: Long?,
        @RequestParam fileName: String,
        @RequestPart file: MultipartFile,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<Long>> {
        val userId = extractUserId(userDetails)
        val itemId = teamCloudService.uploadTeamCloudFile(userId, folderId, file, fileName)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(itemId, HttpStatus.CREATED, "파일이 성공적으로 업로드되었습니다."))
    }

    @PostMapping("/links")
    fun createLink(
        @RequestParam(required = false) folderId: Long?,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: TeamCloudLinkCreateRequest
    ): ResponseEntity<BasicResponse<Long>> {
        val userId = extractUserId(userDetails)
        val itemId = teamCloudService.createTeamCloudLink(userId, folderId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(itemId, HttpStatus.CREATED, "링크가 성공적으로 등록되었습니다."))
    }

    @DeleteMapping("/folders/{folderId}")
    fun deleteFolder(
        @PathVariable folderId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<Nothing?>> {
        val userId = extractUserId(userDetails)
        teamCloudService.deleteFolder(userId, folderId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(null, HttpStatus.OK, "폴더가 성공적으로 삭제되었습니다."))
    }

    @DeleteMapping("/items")
    fun deleteItems(
        @RequestBody request: TeamCloudItemsDeleteRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<Nothing?>> {
        val userId = extractUserId(userDetails)
        teamCloudService.deleteItems(userId, request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(null, HttpStatus.OK, "팀 클라우드 아이템을 성공적으로 삭제했습니다."))
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        return userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    }
}