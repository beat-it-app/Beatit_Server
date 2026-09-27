package com.beat_it.performance.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.performance.dto.*
import com.beat_it.performance.entity.enum.PerformanceFilterStatus
import com.beat_it.performance.service.PerformanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Tag(name = "6. PERFORMANCE API", description = "공연 생성, 조회, 수정, 삭제 및 상태 관리 API")
@RestController
@RequestMapping("/performances")
class PerformanceController(
    private val performanceService: PerformanceService
) {

    @Operation(summary = "공연 등록하기", description = "공연 정보(JSON), 포스터 이미지(선택), 상세 이미지(0~5장 선택)를 등록합니다.")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createPerformance(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestPart("performanceCreateRequest") performanceCreateRequest: PerformanceCreateRequest,
        @Parameter(description = "포스터 이미지 (선택)", required = false)
        @RequestPart(value = "posterImage", required = false) posterImage: MultipartFile?,
        @Parameter(description = "상세 이미지 목록 (0~5장 선택)", required = false)
        @RequestPart(value = "detailImages", required = false) detailImages: List<MultipartFile>?
    ): ResponseEntity<BasicResponse<PerformanceResponse>> {
        val userId = extractUserId(userDetails)

        val response = performanceService.createPerformance(
            userId = userId,
            request = performanceCreateRequest,
            posterImage = posterImage,
            detailImages = detailImages
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(response, HttpStatus.CREATED, "공연이 성공적으로 등록되었습니다."))
    }

    @Operation(summary = "공연 상세 조회", description = "공연 Public UUID로 공연의 상세 정보(포스터, 상세 이미지들, 장소, 티켓 가격 등)를 조회합니다.")
    @GetMapping("/{publicId}")
    fun getPerformanceDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable publicId: UUID
    ): ResponseEntity<BasicResponse<PerformanceResponse>> {
        val userId = extractUserId(userDetails)

        val response = performanceService.getPerformanceDetail(userId, publicId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "공연 상세 정보를 성공적으로 조회했습니다."))
    }

    @Operation(summary = "모바일 초대장 조회", description = "공연 Public UUID로 모바일 초대장 정보(공연명, 일시, 장소명, 설명)를 조회합니다.")
    @GetMapping("/{publicId}/invitation")
    fun getPerformanceInvitation(
        @PathVariable publicId: UUID
    ): ResponseEntity<BasicResponse<PerformanceInvitationResponse>> {
        val response = performanceService.getPerformanceInvitation(publicId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "모바일 초대장 정보를 성공적으로 조회했습니다."))
    }

    @Operation(summary = "나의 공연 목록 조회", description = "우리 팀 소속의 공연 목록을 페이징 조회합니다. (상태 필터링 및 제목 검색 가능)")
    @GetMapping("/my")
    fun getMyPerformanceList(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "ALL") filter: PerformanceFilterStatus,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<BasicResponse<PerformanceListResponse>> {
        val userId = extractUserId(userDetails)

        val response = performanceService.getMyPerformanceList(
            userId = userId,
            filter = filter,
            keyword = keyword,
            page = page,
            size = size
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "나의 공연 목록 조회를 성공했습니다."))
    }

    @Operation(summary = "전체 공연 목록 조회", description = "전체 공연 목록을 페이징 조회합니다. (상태 필터링 및 제목 검색 가능)")
    @GetMapping
    fun getPerformanceList(
        @RequestParam(defaultValue = "ALL") filter: PerformanceFilterStatus,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<BasicResponse<PerformanceListResponse>> {
        val response = performanceService.getPerformanceList(
            filter = filter,
            keyword = keyword,
            page = page,
            size = size
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "공연 목록 조회를 성공했습니다."))
    }

    @Operation(summary = "공연 수정하기", description = "공연 정보, 포스터 이미지 교체, 상세 이미지 추가/삭제, 티켓 가격 수정을 처리합니다.")
    @PatchMapping(
        "/{publicId}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun updatePerformance(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable publicId: UUID,
        @Parameter(description = "공연 수정 정보 (선택)", required = false)
        @Valid @RequestPart(value = "request", required = false) request: PerformanceUpdateRequest?,
        @Parameter(description = "포스터 이미지 교체 (선택)", required = false)
        @RequestPart(value = "posterImage", required = false) posterImage: MultipartFile?,
        @Parameter(description = "상세 이미지 추가 목록 (선택)", required = false)
        @RequestPart(value = "detailImages", required = false) detailImages: List<MultipartFile>?
    ): ResponseEntity<BasicResponse<PerformanceResponse>> {
        val userId = extractUserId(userDetails)

        val response = performanceService.updatePerformance(
            userId = userId,
            publicId = publicId,
            request = request ?: PerformanceUpdateRequest(),
            posterImage = posterImage,
            detailImages = detailImages
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(response, HttpStatus.OK, "공연 정보가 성공적으로 수정되었습니다."))
    }

    @Operation(summary = "공연 삭제하기", description = "공연 및 첨부된 포스터/상세 이미지 파일을 삭제합니다.")
    @DeleteMapping("/{publicId}")
    fun deletePerformance(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable publicId: UUID
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)

        performanceService.deletePerformance(userId, publicId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "공연이 성공적으로 삭제되었습니다."))
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        return userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    }
}
