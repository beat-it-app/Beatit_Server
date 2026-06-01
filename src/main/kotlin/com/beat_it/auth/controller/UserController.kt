package com.beat_it.auth.controller

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.global.security.SecurityUtil
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "2. USER API", description = "사용자 정보 관련 로직")
@RestController
@RequestMapping("/users")
class UserController (
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userDetails: UserDetails?) : ResponseEntity<BasicResponse<Map<String, String?>>> {
        if (userDetails == null) {
            throw BusinessException(ErrorCode.UNAUTHORIZED)
        }

        val publicId = userDetails.username
        
        val headerUserPublicId = SecurityUtil.getHeaderUserPublicId()
        val headerTeamPublicId = SecurityUtil.getHeaderTeamPublicId()
        
        val data = mapOf(
            "tokenUserPublicId" to publicId,
            "headerUserPublicId" to headerUserPublicId,
            "headerTeamPublicId" to headerTeamPublicId
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(data, HttpStatus.OK, "현재 사용자 및 팀 아이디 조회가 성공적으로 처리되었습니다."))
    }

    @PostMapping("/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createProfile(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @Parameter(description = "프로필 이름", example = "김빗잇") @RequestParam("name") name: String,
        @RequestPart(value = "profileImage", required = false) profileImage: MultipartFile?
    ): ResponseEntity<BasicResponse<Nothing>> {
        val currentUserId = userDetails?.username
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        userService.createProfile(currentUserId, name, profileImage)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(HttpStatus.CREATED, "프로필이 생성되었습니다."))
    }
}