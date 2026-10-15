package com.beat_it.auth.controller

import com.beat_it.auth.dto.MyPageResponse
import com.beat_it.auth.dto.ScreenModeDto
import com.beat_it.auth.dto.UpdateNameRequest
import com.beat_it.auth.service.MyPageService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "1-3. MY PAGE API", description = "마이페이지 관련 로직")
@RestController
@RequestMapping("/mypage")
class MyPageController (
    private val myPageService: MyPageService,
){
    @Operation(summary = "마이페이지 조회")
    @GetMapping
    fun getMyPage(@AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<MyPageResponse>> {
        val userId = extractUserId(userDetails)

        val data = myPageService.getMyPage(userId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(data, HttpStatus.OK, "마이페이지 불러오기에 성공했습니다."))
    }

    @Operation(summary = "이름 변경")
    @PatchMapping("/profile/name")
    fun updateName(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: UpdateNameRequest
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)

        myPageService.updateName(userId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "이름 변경에 성공했습니다."))
    }

    @Operation(summary = "프로필 이미지 변경")
    @PatchMapping("/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProfileImage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart(value = "image", required = false) image: MultipartFile?,
        @RequestParam(value = "defaultImageId", required = false) defaultImageId: Int?
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)

        myPageService.updateProfileImage(userId, image, defaultImageId)

        return ResponseEntity.ok(
            BasicResponse.success(HttpStatus.OK, "프로필 이미지 변경에 성공했습니다.")
        )
    }

    @Operation(summary = "프로필 이미지 삭제")
    @DeleteMapping("/profile/image")
    fun deleteProfileImage(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)

        myPageService.deleteProfileImage(userId)

        return ResponseEntity.ok(
            BasicResponse.success(HttpStatus.OK, "프로필 이미지가 삭제되었습니다.")
        )
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        return userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    }

    @Operation(summary = "화면 모드 조회")
    @GetMapping("/screen-mode")
    fun getScreenMode(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<ScreenModeDto>> {
        val userId = extractUserId(userDetails)

        val data = myPageService.getScreenMode(userId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(data, HttpStatus.OK, "화면 모드 불러오기에 성공했습니다."))
    }

    @Operation(summary = "화면 모드 변경")
    @PatchMapping("/screen-mode")
    fun updateScreenMode(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: ScreenModeDto
    ): ResponseEntity<BasicResponse<ScreenModeDto>> {
        val userId = extractUserId(userDetails)

        val data = myPageService.updateScreenMode(userId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(data, HttpStatus.OK, "화면 모드 변경에 성공했습니다."))
    }
}