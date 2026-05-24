package com.beat_it.team.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.TeamCreateRequest
import com.beat_it.team.dto.TeamCreateResponse
import com.beat_it.team.dto.TeamDetailResponse
import com.beat_it.team.dto.TeamDetailUpdateRequest
import com.beat_it.team.dto.TeamDetailUpdateResponse
import com.beat_it.team.entity.TeamMemberships
import com.beat_it.team.entity.Teams
import com.beat_it.team.entity.enum.TeamRole
import com.beat_it.team.repository.TeamLinksRepository
import com.beat_it.team.repository.TeamMembershipRepository
import com.beat_it.team.repository.TeamPartsRepository
import com.beat_it.team.repository.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamLinksRepository: TeamLinksRepository,
    private val teamPartsRepository: TeamPartsRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
) {

    @Transactional
    fun createTeam(userid: Long, request: TeamCreateRequest): TeamCreateResponse {
        validateCreateRequest(request)

        val inviteCode = generateInviteCode()

        val team = Teams(
            teamName = request.teamName,
            description = request.description,
            teamType = request.teamType,
            establishedOn = request.establishedOn,
            inviteCode = inviteCode
        )

        val savedTeam = teamRepository.save(team)

        // TODO: 팀 생성자를 TeamMember에 LEADER로 저장해야 함
        // 현재 createTeam 함수에 userId가 없기 때문에, 나중에 Controller에서 userId를 넘겨받는 구조가 필요함
        val leaderTeamMemberships = TeamMemberships(
            team = savedTeam,
            userid = userid,
            teamRole = TeamRole.LEADER,
        )

        teamMembershipRepository.save(leaderTeamMemberships)

        return TeamCreateResponse(
            teamId = savedTeam.teamId!!,
            teamName = savedTeam.teamName,
            description = savedTeam.description,
            inviteCode = savedTeam.inviteCode,
            teamType = savedTeam.teamType,
            teamRole = "LEADER",
            createdAt = savedTeam.createdAt
        )
    }

    @Transactional
    fun updateTeamDetail(
        teamId: Long,
        userId: Long,
        request: TeamDetailUpdateRequest
    ): TeamDetailUpdateResponse {
        val team = findTeamOrThrow(teamId)

        validateTeamUpdatePermission(teamId, userId)

        if (isNotChanged(team, request)) {
            throw BusinessException(ErrorCode.TEAM_NO_CONTENT_TO_UPDATE)
        }

        validateUpdateRequest(request)

        team.updateTeamDetail(
            teamName = request.teamName,
            description = request.description,
            establishedOn = request.establishedOn,
            teamType = team.teamType,

        )

        // TODO: profileImageFileId가 있다면 파일 존재 여부 검증 후 연결
        // TODO: links가 있다면 TeamLinks 엔티티 저장/수정

        return TeamDetailUpdateResponse(
            teamId = team.teamId!!,
            teamName = team.teamName,
            description = team.description,
            establishedOn = team.establishedOn,
            updatedAt = team.updatedAt
        )
    }

    @Transactional
    fun deleteTeam(
        teamId: Long,
        userId: Long
    ) {
        val team = findTeamOrThrow(teamId)

        validateTeamDeletePermission(teamId, userId)

        team.deleteTeam()
    }

    @Transactional(readOnly = true)
    fun getTeamDetail(teamId: Long): TeamDetailResponse {
        val team = findTeamOrThrow(teamId)

        return TeamDetailResponse(
            teamId = team.teamId,
            profileImageUrl = team.profileImageUrl,
            teamName = team.teamName,
            description = team.description,
            establishedOn = team.establishedOn,
            inviteCode = team.inviteCode,
            memberCount = 0,
            createdAt = team.createdAt,
            updatedAt = team.updatedAt,
            links = emptyList(),
            parts = emptyList(),
            archiveCount = 0,
            cloudItemCount = 0
        )
    }

    private fun validateCreateRequest(request: TeamCreateRequest) {
        if (request.teamName.isBlank()) {
            throw BusinessException(ErrorCode.TEAM_NAME_REQUIRED)
        }

        if (request.teamName.length > 100) {
            throw BusinessException(ErrorCode.TEAM_NAME_TOO_LONG)
        }

        if ((request.description?.length ?: 0) > 500) {
            throw BusinessException(ErrorCode.TEAM_DESCRIPTION_TOO_LONG)
        }
    }

    private fun validateUpdateRequest(request: TeamDetailUpdateRequest) {
        if ((request.teamName?.length ?: 0) > 100) {
            throw BusinessException(ErrorCode.TEAM_NAME_TOO_LONG)
        }

        if ((request.description?.length ?: 0) > 500) {
            throw BusinessException(ErrorCode.TEAM_DESCRIPTION_TOO_LONG)
        }
    }


    private fun isNotChanged(team: Teams, request: TeamDetailUpdateRequest): Boolean {
        val isAnyFieldChanged =
                    (request.teamName != team.teamName) ||
                    (request.description != null && request.description != team.description) ||
                    (request.establishedOn != null && request.establishedOn != team.establishedOn)
                    (request.profileImageUrl != null && request.profileImageUrl != team.profileImageUrl)
                    // (request.links != null && request.links != team.links)

        return !(isAnyFieldChanged)
    }

    private fun findTeamOrThrow(teamId: Long): Teams {
        return teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)
    }

    private fun validateTeamUpdatePermission(teamId: Long, userId: Long) {
        // TODO: TeamMemberRepository가 생기면 여기서 LEADER 또는 MANAGER인지 확인
        val membership = teamMembershipRepository.findByTeamIdAndUserIdAndLeaftAtIsNull(teamId, userId) ?: throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)
        if (membership.teamRole != TeamRole.LEADER && membership.teamRole != TeamRole.MANAGER) {
            throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)
        }
    }

    private fun validateTeamDeletePermission(teamId: Long, userId: Long) {
        // TODO: 팀 삭제 권한 검증
        val membership = teamMembershipRepository.findByTeamIdAndUserIdAndLeaftAtIsNull(teamId, userId) ?: throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)
        if (membership.teamRole != TeamRole.LEADER) {
            throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)
        }
    }

    private fun generateInviteCode(): String {
        // TODO: 팀 초대코드 생성법 고안 필요
        // TODO: Redis로 초대코드를 구성하는 블로그 참고하기
        return "BEATIT-" + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(6)
            .uppercase()
    }
}