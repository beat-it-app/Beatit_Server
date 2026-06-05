package com.beat_it.team.controller

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.TeamCreateRequest
import com.beat_it.team.dto.TeamCreateResponse
import com.beat_it.team.dto.TeamDetailUpdateRequest
import com.beat_it.team.dto.TeamDetailUpdateResponse
import com.beat_it.team.service.TeamService
import com.beat_it.global.response.BasicResponse
import com.beat_it.team.dto.JoinTeamRequest
import com.beat_it.team.dto.JoinTeamResponse
import com.beat_it.team.dto.MyTeamListResponse
import com.beat_it.team.dto.UserTeamListResponse
import com.beat_it.team.dto.VerifyCodeResponse
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

    @Operation(summary = "팀 수정하기")
    @PatchMapping
    fun updateTeamDetail(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: TeamDetailUpdateRequest,
    ): ResponseEntity<BasicResponse<TeamDetailUpdateResponse>> {
        val userId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val teamDetail = teamService.getTeamDetail(userId)
        val teamId = teamDetail?.teamId!!

        val responseData = teamService.updateTeamDetail(userId, teamId, request)

        return ResponseEntity.ok(BasicResponse.success(responseData, HttpStatus.OK, "팀 상세 내용이 수정되었습니다."))
    }

    @Operation(summary = "팀 삭제하기")
    @DeleteMapping
    fun deleteTeam(
        @AuthenticationPrincipal userDetails: UserDetails?,
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val teamDetail = teamService.getTeamDetail(userId)
        val teamId = teamDetail?.teamId!!

        teamService.deleteTeam(userId, teamId)

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

        if (teamDetail != null) {
            return ResponseEntity.ok(
                BasicResponse.success(teamDetail, HttpStatus.OK,"팀 상세 내용 조회에 성공했습니다."))
        }

        val userTeams = teamService.getUserTeams(userId)

        return if (userTeams.teams.isEmpty()) {
            ResponseEntity.ok(BasicResponse.success(HttpStatus.OK, "소속된 팀이 없습니다. 팀을 생성하거나 초대코드를 입력하세요."))
        } else {
            ResponseEntity.ok(BasicResponse.success(userTeams, HttpStatus.OK, "선택된 팀이 없어 소속된 팀 리스트를 반환합니다."))
        }
    }

    @Operation(summary = "로그인할 팀 선택하기")
    @PostMapping("/select")
    fun selectTeam(@AuthenticationPrincipal userDetails: UserDetails?,
    ): ResponseEntity<BasicResponse<Nothing>> {
        val userId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val teamDetail = teamService.getTeamDetail(userId)
        val teamId = teamDetail?.teamId!!

        teamService.selectTeam(userId, teamId)

        return ResponseEntity.ok(BasicResponse.success(HttpStatus.OK, "팀이 성공적으로 선택되었습니다."))
    }

    @PostMapping( "/join")
    fun postJoinTeam(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: JoinTeamRequest,
    ): ResponseEntity<BasicResponse<JoinTeamResponse>> {
        val userId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = teamService.joinTeam(userId, request.inviteCode)

        return ResponseEntity.ok(
            BasicResponse.success(responseData, HttpStatus.OK, "팀 가입이 완료되었습니다.")
        )
    }

    @GetMapping("/me")
    fun getMyTeams(
        @AuthenticationPrincipal userDetails: UserDetails?,
    ): ResponseEntity<BasicResponse<UserTeamListResponse>> {
        val userId = userDetails?.username?.toLong()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = teamService.getUserTeams(userId)

        return ResponseEntity.ok(
            BasicResponse.success(responseData, HttpStatus.OK, "나의 팀 리스토 조회에 성공했습니다.")
        )
    }

    @GetMapping("/verify")
    fun getVerifyCode(
        @RequestParam("inviteCode") inviteCode: String,
    ): ResponseEntity<BasicResponse<VerifyCodeResponse>> {
        val responseData = teamService.getVerifyCode(inviteCode)

        return ResponseEntity.ok(
            BasicResponse.success(
                responseData, HttpStatus.OK, "팀 초대 링크 조회에 성공했습니다."
            )
        )
    }
}

