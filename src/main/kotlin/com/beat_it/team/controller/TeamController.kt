package com.beat_it.team.controller

import com.beat_it.team.dto.TeamCreateRequest
import com.beat_it.team.dto.TeamCreateResponse
import com.beat_it.team.dto.TeamDetailResponse
import com.beat_it.team.dto.TeamDetailUpdateRequest
import com.beat_it.team.dto.TeamDetailUpdateResponse
import com.beat_it.team.service.TeamService
import com.beat_it.global.response.BasicResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService
) {

    @PostMapping
    fun createTeam(
        @RequestHeader("X-User-Public-Id") userPublicId: UUID,
        @RequestBody request: TeamCreateRequest
    ): ResponseEntity<BasicResponse<TeamCreateResponse>> {

        val responseData = teamService.createTeam(userPublicId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.success(responseData, HttpStatus.CREATED, "팀 생성에 성공했습니다."))
    }

    @PatchMapping
    fun updateTeamDetail(
        @RequestHeader("X-Team-Public-Id") teamPublicId: UUID,
        @RequestHeader("X-User-Public-Id") userPublicId: UUID,
        @RequestBody request: TeamDetailUpdateRequest,
    ): ResponseEntity<BasicResponse<TeamDetailUpdateResponse>> {
        val responseData = teamService.updateTeamDetail(teamPublicId, userPublicId, request)
        return ResponseEntity.ok(BasicResponse.success(responseData, HttpStatus.OK, "팀 상세 내용이 수정되었습니다."))
    }

    @DeleteMapping
    fun deleteTeam(
        @RequestHeader("X-Team-Public-Id") teamPublicId: UUID,
        @RequestHeader("X-User-Public-Id") userPublicId: UUID,
    ): ResponseEntity<BasicResponse<Nothing>> {
        teamService.deleteTeam(teamPublicId, userPublicId)
        return ResponseEntity.ok(
            BasicResponse.success(HttpStatus.OK,"팀이 성공적으로 삭제되었습니다.")
        )
    }

    @GetMapping
    fun getTeamDetail(
        @RequestHeader("X-Team-Public-Id") teamPublicId: UUID,
    ): ResponseEntity<BasicResponse<TeamDetailResponse>> {
        val responseData = teamService.getTeamDetail(teamPublicId)
        return ResponseEntity.ok(
            BasicResponse.success(responseData, HttpStatus.OK,"팀 상세 내용 조회에 성공했습니다.")
        )
    }
}

