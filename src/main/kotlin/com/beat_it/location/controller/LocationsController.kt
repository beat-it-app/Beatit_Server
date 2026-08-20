package com.beat_it.location.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.response.BasicResponse
import com.beat_it.location.dto.LocationRequest
import com.beat_it.location.dto.LocationResponse
import com.beat_it.location.dto.LocationSearchResponse
import com.beat_it.location.service.LocationsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

import java.math.BigDecimal

@Tag(name = "7. LOCATION API", description = "장소 등록 및 조회 API")
@RestController
@RequestMapping("/locations")
class LocationsController(
    private val locationsService: LocationsService
) {

    @Operation(summary = "장소 등록하기 (지도 업로드)")
    @PostMapping
    fun createLocation(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: LocationRequest
    ): ResponseEntity<BasicResponse<LocationResponse>> {
        val userId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val (responseData, isCreated) = locationsService.createLocation(userId, request)
        
        val message = if (isCreated) "장소가 성공적으로 등록되었습니다." else "이미 등록된 장소입니다."
        val status = if (isCreated) HttpStatus.CREATED else HttpStatus.OK

        return ResponseEntity
            .status(status)
            .body(BasicResponse.success(responseData, status, message))
    }

    @Operation(summary = "장소 키워드로 검색하기 (카카오 로컬 API 프록시)")
    @GetMapping("/search")
    fun searchLocations(
        @RequestParam query: String,
        @RequestParam(required = false) latitude: BigDecimal?,
        @RequestParam(required = false) longitude: BigDecimal?
    ): ResponseEntity<BasicResponse<List<LocationSearchResponse>>> {
        val responseData = locationsService.searchLocations(query, latitude, longitude)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "장소 키워드 검색에 성공했습니다."))
    }

    @Operation(summary = "장소 상세 조회하기")
    @GetMapping("/{locationId}")
    fun getLocation(
        @PathVariable locationId: Long
    ): ResponseEntity<BasicResponse<LocationResponse>> {
        val responseData = locationsService.getLocation(locationId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "장소 조회에 성공했습니다."))
    }
}
