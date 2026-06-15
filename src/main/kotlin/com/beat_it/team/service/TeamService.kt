package com.beat_it.team.service

import com.beat_it.auth.entity.Users
import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.*
import com.beat_it.team.entity.TeamLinks
import com.beat_it.team.entity.TeamMemberships
import com.beat_it.team.entity.Teams
import com.beat_it.team.entity.enum.TeamRole
import com.beat_it.team.repository.TeamLinksRepository
import com.beat_it.team.repository.TeamMembershipRepository
import com.beat_it.team.repository.TeamPartsRepository
import com.beat_it.team.repository.TeamRepository
import org.springframework.data.repository.findByIdOrNull
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
    fun createTeam(userId: Long, request: TeamCreateRequest): TeamCreateResponse {
        val user = findUserOrThrow(userId)

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

        val leaderTeamMemberships = TeamMemberships(
            team = savedTeam,
            userId = user.userId!!,
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
        userId: Long,
        request: TeamDetailUpdateRequest
    ): TeamDetailUpdateResponse {
        val user = findUserOrThrow(userId)
        val teamId = user.currentTeamId
            ?: throw BusinessException(ErrorCode.TEAM_NOT_SELECTED)

        val team = findTeamForCommandOrThrow(teamId)

        val currentLinks = teamLinksRepository.findAllByTeamTeamId(team.teamId!!)

        validateTeamUpdatePermission(team.teamId!!, user.userId!!)
        validateUpdateRequest(request)
        validateTeamDetailChanged(team, request, currentLinks)

        team.updateTeamDetail(
            teamName = request.teamName,
            description = request.description,
            establishedOn = request.establishedOn,
            teamType = request.teamType,
        )

        request.teamImageUrl?.let {
            team.teamImageUrl = it
        }

        request.links?.let { linkRequests ->
            teamLinksRepository.deleteAllByTeamTeamId(teamId)

            val newLinks = linkRequests.map { linkRequest ->
                TeamLinks(
                    team = team,
                    platformCode = linkRequest.platformCode,
                    linkUrl = linkRequest.linkUrl
                )
            }

            teamLinksRepository.saveAll(newLinks)
        }

        val links = teamLinksRepository.findAllByTeamTeamId(teamId)
            .map {
                LinksResponse(
                    teamLinkId = it.teamLinkId!!,
                    platformCode = it.platformCode,
                    linkUrl = it.linkUrl,
                )
            }

        return TeamDetailUpdateResponse(
            teamId = teamId,
            teamName = team.teamName,
            description = team.description,
            establishedOn = team.establishedOn,
            updatedAt = team.updatedAt,
            links = links
        )
    }

    @Transactional
    fun deleteTeam(
        userId: Long,
        teamId: Long
    ) {
        val user = findUserOrThrow(userId)
        val team = findTeamForCommandOrThrow(teamId)

        validateTeamDeletePermission(team.teamId!!, user.userId!!)

        //TODO: user.currentTeamId가 teamId와 같은 모든 회원의 currentTeamId도 null 처리해야 함.
        userRepository.clearCurrentTeamIdByTeamId(teamId)

        //TODO: 유효기간 관련 처리

        team.delete()
    }

    @Transactional(readOnly = true)
    fun getTeamDetail(userId: Long): TeamDetailResponse? {
        val user = findUserOrThrow(userId)

        val teamId = user.currentTeamId ?: return null

        val team = teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
            ?: return null

        val memberCount = teamMembershipRepository.countByTeamTeamIdAndLeftAtIsNull(teamId)

        val links = teamLinksRepository
            .findAllByTeamTeamId(teamId)
            .map {
                LinksResponse(
                    teamLinkId = it.teamLinkId!!,
                    platformCode = it.platformCode,
                    linkUrl = it.linkUrl,
                )
            }

        val parts = teamPartsRepository
            .findAllByTeamTeamId(teamId)
            .map {
                PartsResponse(
                    teamPartId = it.teamPartId!!,
                    partName = it.partName,
                    displayOrder = it.displayOrder,
                )
            }

        return TeamDetailResponse(
            teamId = team.teamId,
            teamImageUrl = team.teamImageUrl,
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

    @Transactional
    fun joinTeam(userId: Long, inviteCode: String?): JoinTeamResponse {
        val normalizedInviteCode = validateAndNormalizeInviteCode(inviteCode)
        val user = findUserOrThrow(userId)

        val team = teamRepository.findByInviteCodeAndDeletedAtIsNull(normalizedInviteCode)
            ?: throw BusinessException(ErrorCode.TEAM_INVITE_CODE_NOT_FOUND)

        validateNotAlreadyJoined(team.teamId!!, user.userId!!)

        val teamMembership = TeamMemberships(
            team = team,
            userId = user.userId!!,
            teamRole = TeamRole.MEMBER
        )

        val savedMembership = teamMembershipRepository.save(teamMembership)

        return JoinTeamResponse(
            teamId = team.teamId!!,
            teamName = team.teamName,
            teamRole = savedMembership.teamRole,
            joinedAt = savedMembership.createdAt
        )
    }

    @Transactional(readOnly = true)
    fun getUserTeams(userId: Long) : UserTeamListResponse {
        val user = findUserOrThrow(userId)

        val memberships = teamMembershipRepository.findAllByUserIdAndLeftAtIsNullAndTeamDeletedAtIsNullOrderByCreatedAtDesc(user.userId!!)

        val teams = memberships.map { membership ->
            val team = membership.team
            TeamSimpleInfo(
                teamId = team.teamId!!,
                teamName = team.teamName,
                teamType = team.teamType,
                teamImageUrl = team.teamImageUrl,
                createAt = team.createdAt.toLocalDate()
            )
        }

        return UserTeamListResponse(teams = teams)
    }

    @Transactional(readOnly = true)
    fun getTeamInfoByInviteCode(inviteCode: String): TeamSimpleInfo {
        val normalizedInviteCode = validateAndNormalizeInviteCode(inviteCode)

        val team = teamRepository.findByInviteCodeAndDeletedAtIsNull(normalizedInviteCode)
            ?: throw BusinessException(ErrorCode.TEAM_INVITE_CODE_NOT_FOUND)

        return TeamSimpleInfo(
            teamId = team.teamId!!,
            teamName = team.teamName,
            teamType = team.teamType,
            teamImageUrl = team.teamImageUrl,
            createAt = team.createdAt.toLocalDate(),
        )
    }

    @Transactional
    fun selectTeam(userId: Long, teamId: Long) {
        val user = findUserOrThrow(userId)
        val team = findTeamOrThrow(teamId)

        teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(
            team.teamId!!,
            user.userId!!
        ) ?: throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)

        user.updateCurrentTeam(team.teamId!!)
    }

    private fun findUserOrThrow(userId: Long) : Users {
        return userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    private fun findTeamOrThrow(teamId: Long): Teams {
        return teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)
    }

    private fun findTeamForCommandOrThrow(teamId: Long): Teams {
        val team = teamRepository.findByTeamId(teamId)
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)

        if (team.deletedAt != null) {
            throw BusinessException(ErrorCode.TEAM_PENDING_DELETION)
        }

        return team
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
        if (request.teamName != null && request.teamName.isBlank()) {
            throw BusinessException(ErrorCode.TEAM_NAME_REQUIRED)
        }

        if ((request.description?.length ?: 0) > 500) {
            throw BusinessException(ErrorCode.TEAM_DESCRIPTION_TOO_LONG)
        }
    }

    private fun validateTeamDetailChanged(
        team: Teams,
        request: TeamDetailUpdateRequest,
        currentLinks: List<TeamLinks>
    ) {
        val isAnyFieldChanged =
            (request.teamName != null && request.teamName != team.teamName) ||
                    (request.description != null && request.description != team.description) ||
                    (request.establishedOn != null && request.establishedOn != team.establishedOn) ||
                    (request.teamType != null && request.teamType != team.teamType) ||
                    (request.teamImageUrl != null && request.teamImageUrl != team.teamImageUrl)

        val isLinksChanged =
            request.links != null && !isLinksSame(currentLinks, request.links)

        if (!isAnyFieldChanged && !isLinksChanged) {
            throw BusinessException(ErrorCode.TEAM_NO_CONTENT_TO_UPDATE)
        }
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

    private fun validateAndNormalizeInviteCode(inviteCode: String?): String {
        if(inviteCode.isNullOrBlank()) {
            throw BusinessException(ErrorCode.TEAM_INVITE_CODE_REQUIRED)
        }

        val normalizedInviteCode = inviteCode.trim().uppercase()

        val inviteCodeRegex = Regex("^[A-Z0-9]{6}$")

        if (!inviteCodeRegex.matches(normalizedInviteCode)) {
            throw BusinessException(ErrorCode.TEAM_INVITE_CODE_INVALID)
        }

        return normalizedInviteCode
    }

    private fun validateNotAlreadyJoined(teamId: Long, userId: Long) {
        val alreadyJoined = teamMembershipRepository
            .existsByTeamTeamIdAndUserIdAndLeftAtIsNull(teamId, userId)

        if (alreadyJoined) {
            throw BusinessException(ErrorCode.TEAM_ALREADY_JOINED)
        }
    }

    private fun generateInviteCode(): String {
        // TODO: 팀 초대코드 생성법 고안 필요
        // TODO: Redis로 초대코드를 구성하는 블로그 참고하기
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(6)
            .uppercase()
    }
}