package com.beat_it.team.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.TeamCreateRequest
import com.beat_it.team.dto.TeamCreateResponse
import com.beat_it.team.dto.TeamDetailUpdateRequest
import com.beat_it.team.dto.TeamDetailUpdateResponse
import com.beat_it.team.service.TeamService
import com.beat_it.global.response.BasicResponse
import com.beat_it.team.repository.TeamRepository
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails

@Tag(name = "3. TEAM API", description = "팀 생성 및 수정 관련 로직")
@RestController
@RequestMapping("/teams")
@SecurityRequirements
class TeamController(
    private val teamService: TeamService,
    private val teamRepository: TeamRepository
) {

    @Operation(summary = "팀 생성하기")
    @PostMapping
    fun createTeam(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: TeamCreateRequest
    ): ResponseEntity<BasicResponse<TeamCreateResponse>> {
        val userId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = teamService.createTeam(userId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(responseData, HttpStatus.CREATED, "팀 생성에 성공했습니다."))
    }


    @PatchMapping
    fun updateTeamDetail(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestParam("teamPublicId") teamPublicId: UUID,

        @RequestBody request: TeamDetailUpdateRequest,
    ): ResponseEntity<BasicResponse<TeamDetailUpdateResponse>> {
        val userPublicId = UUID.fromString(userDetails?.username)
        val responseData = teamService.updateTeamDetail(teamPublicId, userPublicId, request)

        return ResponseEntity.ok(BasicResponse.success(responseData, HttpStatus.OK, "팀 상세 내용이 수정되었습니다."))
    }

    @DeleteMapping
    fun deleteTeam(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestParam("teamPublicId") teamPublicId: UUID,
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userPublicId = UUID.fromString(userDetails?.username)
        teamService.deleteTeam(teamPublicId, userPublicId)

        return ResponseEntity.ok(
            BasicResponse.success(HttpStatus.OK,"팀이 성공적으로 삭제되었습니다.")
        )
    }

    @Operation(summary = "팀 페이지 불러오기")
    @GetMapping
    fun getTeamDetail(@AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<BasicResponse<out Any>> {
        val userId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val teamDetail = teamService.getTeamDetail(userId)

        // 선택된 팀이 있다면 상세 정보 반환
        if (teamDetail != null) {
            return ResponseEntity.ok(
                BasicResponse.success(teamDetail, HttpStatus.OK,"팀 상세 내용 조회에 성공했습니다."))
        }

        val userTeams = teamService.getUserTeams(userId)

        // 선택된 팀이 없음
        return if (userTeams.teams.isEmpty()) { // 소속된 팀조차 없음
            ResponseEntity.ok(BasicResponse.success(HttpStatus.OK, "소속된 팀이 없습니다. 팀을 생성하거나 초대코드를 입력하세요."))
        } else { // 소속된 팀 리스트는 있음
            ResponseEntity.ok(BasicResponse.success(userTeams, HttpStatus.OK, "선택된 팀이 없어 소속된 팀 리스트를 반환합니다."))
        }
    }

    @Operation(summary = "로그인할 팀 선택하기")
    @PostMapping("/select")
    fun selectTeam(@AuthenticationPrincipal userDetails: UserDetails?,
                          @RequestParam("teamPublicId") teamPublicId: UUID
    ): ResponseEntity<BasicResponse<Nothing>> {
        val currentUserId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val team = teamRepository.findByPublicId(teamPublicId)
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)

        teamService.selectTeam(currentUserId, team.teamId!!)

        return ResponseEntity.ok(BasicResponse.success(HttpStatus.OK, "팀이 성공적으로 선택되었습니다."))
    }
}

