package com.beat_it.team.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.service.TeamService
import com.beat_it.cal.service.ScheduleService
import com.beat_it.performance.service.PerformanceService
import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.beat_it.global.response.BasicResponse
import com.beat_it.team.dto.*
import com.beat_it.team.dto.teamMember.*
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.UUID
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails

@Tag(name = "2-1. TEAM API", description = "팀 관련 로직")
@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService,
    private val scheduleService: ScheduleService,
    private val performanceService: PerformanceService,
    private val objectMapper: ObjectMapper,
) {

    @Operation(summary = "팀 생성하기")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createTeam(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam teamName: String,
        @RequestParam(required = false) description: String?,
        @RequestParam teamType: TeamType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) establishedOn: LocalDate?,
        @RequestPart(value = "teamImage", required = false) teamImage: MultipartFile?,
    ): ResponseEntity<BasicResponse<TeamCreateResponse>> {
        val request = TeamCreateRequest(teamName, description, teamType, establishedOn)
        val response = teamService.createTeam(extractUserId(userDetails), request, teamImage)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BasicResponse.success(response, HttpStatus.CREATED, "팀이 성공적으로 생성되었습니다."))
    }

    @Operation(summary = "팀 수정하기")
    @PatchMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateTeamDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) teamName: String?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) teamType: TeamType?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) establishedOn: LocalDate?,
        @RequestParam(required = false) links: String?,
        @RequestPart(value = "teamImage", required = false) teamImage: MultipartFile?,
    ): ResponseEntity<BasicResponse<TeamDetailUpdateResponse>> {
        val request = TeamDetailUpdateRequest(
            teamName = teamName,
            description = description,
            teamType = teamType,
            establishedOn = establishedOn,
            links = links?.let {
                try {
                    objectMapper.readValue(it, object : TypeReference<List<TeamLinksRequest>>() {})
                } catch (e: com.fasterxml.jackson.core.JsonProcessingException) {
                    throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
                }
            },
        )
        val response = teamService.updateTeamDetail(extractUserId(userDetails), request, teamImage)
        return ResponseEntity.ok(BasicResponse.success(response, HttpStatus.OK, "팀 상세 내용이 성공적으로 수정되었습니다."))
    }

    @Operation(summary = "팀 삭제하기")
    @DeleteMapping("/{teamPublicId}")
    fun deleteTeam(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable teamPublicId: UUID
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)
        teamService.deleteTeam(userId, teamPublicId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "팀이 성공적으로 삭제되었습니다."))
    }

    @Operation(summary = "팀 페이지 불러오기")
    @GetMapping
    fun getTeamDetail(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<TeamDetailResponse>> {
        val userId = extractUserId(userDetails)
        val responseData = teamService.getTeamDetail(userId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 상세 내용 조회에 성공했습니다."))
    }

    @Operation(summary = "팀의 일주일 일정 조회")
    @GetMapping("/calendars")
    fun getTeamCalendars(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<BasicResponse<TeamCalendarsResponse>> {
        val userId = extractUserId(userDetails)
        val schedules = scheduleService.getUpcomingTeamSchedules(userId)
        val responseData = TeamCalendarsResponse(
            items = schedules.map { schedule ->
                TeamCalendarItemResponse(
                    scheduleId = schedule.scheduleId,
                    title = schedule.title,
                    startsAt = schedule.startsAt,
                    endsAt = schedule.endsAt,
                    locationName = schedule.locationName,
                )
            }
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 일정 조회에 성공했습니다."))
    }

    @Operation(summary = "팀의 다가오는 공연 조회")
    @GetMapping("/performances")
    fun getTeamPerformances(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<BasicResponse<TeamPerformancesResponse>> {
        val userId = extractUserId(userDetails)
        val performances = performanceService.getUpcomingTeamPerformances(userId)
        val responseData = TeamPerformancesResponse(
            items = performances.map { performance ->
                TeamPerformanceItemResponse(
                    performancePublicId = performance.performancePublicId,
                    title = performance.title,
                    performanceDateTime = performance.performanceDateTime,
                    posterImageUrl = performance.posterImageUrl,
                )
            }
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 공연 조회에 성공했습니다."))
    }

    @Operation(summary = "로그인할 팀 선택하기")
    @PostMapping("/select/{teamPublicId}")
    fun selectTeam(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable teamPublicId: UUID,
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = extractUserId(userDetails)

        teamService.selectTeam(userId, teamPublicId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(HttpStatus.OK, "팀이 성공적으로 선택되었습니다."))
    }

    @Operation(summary = "팀 Public ID로 팀 가입하기")
    @PostMapping( "/join/{teamPublicId}")
    fun postJoinTeam(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable teamPublicId: UUID,
    ): ResponseEntity<BasicResponse<TeamJoinResponse>> {
        val userId = extractUserId(userDetails)

        val responseData = teamService.joinTeam(userId, teamPublicId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 가입이 완료되었습니다."))
    }

    @Operation(summary = "내 팀 목록 확인하기")
    @GetMapping("/me")
    fun getMyTeams(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<BasicResponse<UserTeamListResponse>> {
        val userId = extractUserId(userDetails)

        val responseData = teamService.getUserTeams(userId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "나의 팀 리스트 조회에 성공했습니다."))
    }

    @Operation(summary = "초대코드의 팀 정보 조회")
    @GetMapping("/verify/{inviteCode}")
    fun getVerifyCode(
        @PathVariable inviteCode: String,
    ): ResponseEntity<BasicResponse<TeamInviteInfoResponse>> {
        val responseData = teamService.getTeamInfoByInviteCode(inviteCode)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 초대코드 조회에 성공했습니다."))
    }

    @Operation(summary = "멤버 목록 확인하기")
    @GetMapping("/members")
    fun getMembers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<BasicResponse<TeamMemberListResponse>>{
        val userId = extractUserId(userDetails)

        val responseData = teamService.getTeamMembers(userId, query, page, size)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 멤버 목록 조회에 성공했습니다."))
    }

    @Operation(summary = "팀 멤버 포지션 목록 조회")
    @GetMapping("/members/position")
    fun getTeamMemberPositions(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<BasicResponse<TeamMemberPositionResponse>> {
        val userId = extractUserId(userDetails)

        val responseData = teamService.getTeamMemberPositions(userId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 멤버 포지션 목록 조회에 성공했습니다."))
    }

    @Operation(summary = "팀 멤버 포지션 수정하기")
    @PatchMapping("/members/position")
    fun updateTeamMemberPositions(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody @Valid request: TeamMemberPositionUpdateRequest,
    ): ResponseEntity<BasicResponse<TeamMemberPositionResponse>> {
        val userId = extractUserId(userDetails)

        val responseData = teamService.updateTeamMemberPositions(userId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "팀 멤버 포지션이 성공적으로 수정되었습니다."))
    }

    @Operation(summary = "멤버 권한 바꾸기")
    @PostMapping("/members/{userPublicId}")
    fun postManageMember(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable userPublicId: UUID,
        @RequestBody request: TeamManageRequest,
    ): ResponseEntity<BasicResponse<TeamManageResponse>> {
        val userId = extractUserId(userDetails)

        val responseData = teamService.updateMemberRole(request, userId, userPublicId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseData, HttpStatus.OK, "멤버 권한이 성공적으로 변경되었습니다."))
    }

    @Operation(summary = "팀 탈퇴하기")
    @PostMapping("/withdraw/{teamPublicId}")
    fun withdrawTeam(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable teamPublicId: UUID
    ): ResponseEntity<BasicResponse<com.beat_it.team.dto.TeamWithdrawalResponse>> {
        val userId = extractUserId(userDetails)

        val responseData = teamService.teamWithdraw(userId, teamPublicId)

        return ResponseEntity.ok(
            BasicResponse.success(responseData, HttpStatus.OK, "팀 탈퇴 요청이 정상적으로 접수되었습니다. 7일의 유예기간 후 완전히 탈퇴 처리됩니다.")
        )
    }

    private fun extractUserId(userDetails: UserDetails): Long {
        return userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    }
}

