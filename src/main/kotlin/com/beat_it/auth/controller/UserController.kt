package com.beat_it.auth.controller

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
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

@Tag(name = "1-2. USER API", description = "사용자 정보 관련 로직")
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

        val userId = userDetails.username
        
        val data = mapOf(
            "tokenUserId" to userId
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(data, HttpStatus.OK, "현재 사용자 조회가 성공적으로 처리되었습니다."))
    }

    @Operation(summary = "프로필 생성하기")
    @PostMapping("/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "프로필 이름", example = "김빗잇") @RequestParam("name") name: String,
        @RequestPart(value = "profileImage", required = false) profileImage: MultipartFile?,
        @RequestParam(value = "defaultImageId", required = false) defaultImageId: Int?
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        userService.createProfile(userId, name, profileImage, defaultImageId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(HttpStatus.CREATED, "프로필이 생성되었습니다."))
    }
}