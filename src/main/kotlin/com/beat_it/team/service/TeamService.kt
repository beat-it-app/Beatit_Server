package com.beat_it.team.service

import com.beat_it.auth.entity.Users
import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.LinksResponse
import com.beat_it.team.dto.PartsResponse
import com.beat_it.team.dto.TeamCreateRequest
import com.beat_it.team.dto.TeamCreateResponse
import com.beat_it.team.dto.TeamDetailResponse
import com.beat_it.team.dto.TeamDetailUpdateRequest
import com.beat_it.team.dto.TeamDetailUpdateResponse
import com.beat_it.team.dto.TeamLinksRequest
import com.beat_it.team.entity.TeamLinks
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
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val teamLinksRepository: TeamLinksRepository,
    private val teamPartsRepository: TeamPartsRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
) {

    @Transactional
    fun createTeam(userPublicId: UUID, request: TeamCreateRequest): TeamCreateResponse {
        validateCreateRequest(request)

        val userId = findUserOrThrow(userPublicId).userId!!

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
            userId = userId,
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
        teamPublicId: UUID,
        userPublicId: UUID,
        request: TeamDetailUpdateRequest
    ): TeamDetailUpdateResponse {
        val team = findTeamOrThrow(teamPublicId)
        val user = findUserOrThrow(userPublicId)
        val currentLinks = teamLinksRepository.findAllByTeamTeamId(team.teamId!!)

        validateTeamUpdatePermission(team.teamId!!, user.userId!!)
        validateUpdateRequest(request)

        if (isNotChanged(team, request, currentLinks)) {
            throw BusinessException(ErrorCode.TEAM_NO_CONTENT_TO_UPDATE)
        }

        team.updateTeamDetail(
            teamName = request.teamName,
            description = request.description,
            establishedOn = request.establishedOn,
            teamType = team.teamType,
        )

        request.profileImageUrl?.let {
            team.profileImageUrl = it
        }

        request.links?.let { linkRequests ->
            teamLinksRepository.deleteAllByTeamTeamId(team.teamId!!)

            val newLinks = linkRequests.map { linkRequest ->
                TeamLinks(
                    team = team,
                    platformCode = linkRequest.platformCode,
                    linkUrl = linkRequest.linkUrl
                )
            }

            teamLinksRepository.saveAll(newLinks)
        }

        val links = teamLinksRepository.findAllByTeamTeamId(team.teamId!!)
            .map {
                LinksResponse(
                    teamLinkId = it.teamLinkId!!,
                    platFormCode = it.platformCode,
                    linkUrl = it.linkUrl,
                )
            }

        return TeamDetailUpdateResponse(
            teamId = team.teamId!!,
            teamName = team.teamName,
            description = team.description,
            establishedOn = team.establishedOn,
            updatedAt = team.updatedAt,
            links = links
        )
    }

    @Transactional
    fun deleteTeam(
        teamPublicId: UUID,
        userPublicId: UUID,
    ) {
        val team = findTeamOrThrow(teamPublicId)
        val user = findUserOrThrow(userPublicId)

        validateTeamDeletePermission(team.teamId!!, user.userId!!)

        team.deleteTeam()
    }

    @Transactional(readOnly = true)
    fun getTeamDetail(teamPublicId: UUID): TeamDetailResponse {
        val team = findTeamOrThrow(teamPublicId)

        val memberCount = teamMembershipRepository.countByTeamTeamIdAndLeftAtIsNull(team.teamId!!)

        val links = teamLinksRepository
            .findAllByTeamTeamId(team.teamId!!)
            .map {
                LinksResponse(
                    teamLinkId = it.teamLinkId!!,
                    platFormCode = it.platformCode,
                    linkUrl = it.linkUrl,
                )
            }

        val parts = teamPartsRepository
            .findAllByTeamTeamId(team.teamId!!)
            .map {
                PartsResponse(
                    teamPartId = it.teamPartId!!,
                    partName = it.partName,
                    displayOrder = it.displayOrder,
                )
            }

        return TeamDetailResponse(
            teamId = team.teamId,
            profileImageUrl = team.profileImageUrl,
            teamName = team.teamName,
            description = team.description,
            establishedOn = team.establishedOn,
            inviteCode = team.inviteCode,
            memberCount = memberCount,
            createdAt = team.createdAt,
            updatedAt = team.updatedAt,
            links = links,
            parts = parts,
            archiveCount = 0,
            cloudItemCount = 0
        )
    }

    private fun findUserOrThrow(userPublicId: UUID) : Users {
        return userRepository.findByPublicId(userPublicId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    private fun findTeamOrThrow(teamPublicId: UUID): Teams {
        return teamRepository.findByPublicId(teamPublicId)
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)
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

    private fun isNotChanged(
        team: Teams,
        request: TeamDetailUpdateRequest,
        currentLinks: List<TeamLinks>
    ): Boolean {
        val isAnyFieldChanged =
            (request.teamName != team.teamName) ||
            (request.description != null && request.description != team.description) ||
            (request.establishedOn != null && request.establishedOn != team.establishedOn) ||
            (request.teamType != null && request.teamType != team.teamType) ||
            (request.profileImageUrl != null && request.profileImageUrl != team.profileImageUrl)

        val isLinksChanged =
            request.links != null && !isLinksSame(currentLinks, request.links)

        return !(isAnyFieldChanged || isLinksChanged)
    }

    private fun isLinksSame(
        currentLinks: List<TeamLinks>,
        requestLinks: List<TeamLinksRequest>
    ) : Boolean {
        val current = currentLinks
            .map { it.platformCode to it.linkUrl }
            .sortedWith(compareBy({it.first.toString()}, { it.second }))

        val requested = requestLinks
            .map { it.platformCode to it.linkUrl }
            .sortedWith(compareBy({it.first.toString()}, { it.second }))

        return current == requested
    }



    private fun validateTeamUpdatePermission(teamId: Long, userId: Long) {
        // TODO: TeamMemberRepository가 생기면 여기서 LEADER 또는 MANAGER인지 확인
        val membership = teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(teamId, userId) ?: throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)
        if (membership.teamRole != TeamRole.LEADER && membership.teamRole != TeamRole.MANAGER) {
            throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)
        }
    }

    private fun validateTeamDeletePermission(teamId: Long, userId: Long) {
        // TODO: 팀 삭제 권한 검증
        val membership = teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(teamId, userId) ?: throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)
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