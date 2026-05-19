//package com.beat_it.team.controller
//
//import com.beat_it.global.response.BasicResponse
//import com.beat_it.team.dto.TeamCreateRequest
//import com.beat_it.team.dto.TeamCreateResponse
//import com.beat_it.team.dto.TeamListResponse
//import com.beat_it.team.dto.TeamUpdateRequest
//import com.beat_it.team.dto.TeamUpdateResponse
//import com.beat_it.team.service.TeamService
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("/teams")
//class TeamController(
//    private val teamService: TeamService
//) {
//
//    @PostMapping
//    fun createTeam(
//        @RequestHeader("X-USER-ID") userId: Long,
//        @RequestBody request: TeamCreateRequest
//    ): ResponseEntity<BasicResponse<TeamCreateResponse>> {
//
//        val responseData = teamService.createTeam(userId, request)
//
//        return ResponseEntity
//            .status(HttpStatus.CREATED)
//            .body(BasicResponse.success(responseData, "팀 생성에 성공했습니다."))
//    }
//
//    @GetMapping
//    fun getMyTeams(
//        @RequestHeader("X-USER-ID") userId: Long
//    ): ResponseEntity<BasicResponse<TeamListResponse>> {
//
//        val responseData = teamService.getMyTeams(userId)
//
//        return ResponseEntity.ok(
//            BasicResponse.success(responseData, "내 팀 목록 조회에 성공했습니다.")
//        )
//    }
//
//    @PatchMapping
//    fun updateTeamPage(
//        @RequestHeader("X-USER-ID") userId: Long,
//        @RequestHeader(value = "X-TEAM-ID", required = false) teamId: Long?,
//        @RequestBody request: TeamUpdateRequest
//    ): ResponseEntity<BasicResponse<TeamUpdateResponse>> {
//
//        val responseData = teamService.updateTeamPage(userId, teamId, request)
//
//        return ResponseEntity.ok(
//            BasicResponse.success(responseData, "팀 페이지 수정에 성공했습니다.")
//        )
//    }
//}