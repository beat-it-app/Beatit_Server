package com.beat_it.team.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.team.dto.*
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
    private val userService: UserService,
    private val teamRepository: TeamRepository,
    private val teamLinksRepository: TeamLinksRepository,
    private val teamPartsRepository: TeamPartsRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
) {

    @Transactional
    fun createTeam(userId: Long, request: TeamCreateRequest): TeamCreateResponse {
        validateCreateRequest(request)
        userService.validateUserExists(userId)

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
            userId = userId,
            teamRole = TeamRole.LEADER,
        )

        teamMembershipRepository.save(leaderTeamMemberships)

        userService.updateCurrentTeamId(userId, savedTeam.teamId!!)

        return TeamCreateResponse(
            teamId = savedTeam.teamId!!,
            teamPublicId = savedTeam.publicId,
            teamName = savedTeam.teamName,
            description = savedTeam.description,
            inviteCode = savedTeam.inviteCode,
            teamType = savedTeam.teamType,
            teamRole = "LEADER",
            createdAt =  DateTimeUtil.format(savedTeam.createdAt)
        )
    }

    @Transactional
    fun updateTeamDetail(
        userId: Long,
        request: TeamDetailUpdateRequest
    ): TeamDetailUpdateResponse {
        validateUpdateRequest(request)
        val teamId = userService.getCurrentTeamId(userId)

        val team = findTeamForCommandOrThrow(teamId)

        validateTeamUpdatePermission(team.teamId!!, userId)

        val currentLinks = teamLinksRepository.findAllByTeamTeamId(team.teamId!!)

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
            teamPublicId = team.publicId,
            teamName = team.teamName,
            description = team.description,
            establishedOn = team.establishedOn,
            updatedAt = DateTimeUtil.format(team.updatedAt),
            links = links
        )
    }

    @Transactional
    fun deleteTeam(
        userId: Long,
        teamPublicId: UUID
    ) {
        userService.validateUserExists(userId)
        val team = findTeamForCommandOrThrow(teamPublicId)

        validateTeamDeletePermission(team.teamId!!, userId)

        userService.clearCurrentTeamIdByTeamId(team.teamId!!)

        val activeMemberships = teamMembershipRepository
            .findAllByTeamTeamIdAndLeftAtIsNull(team.teamId!!)

        activeMemberships.forEach { it.leaveTeam() }

        //TODO: 유효기간 관련 처리

        team.delete()
    }

    @Transactional(readOnly = true)
    fun getTeamDetail(userId: Long): TeamDetailResponse? {
        val teamId = userService.getCurrentTeamIdOrNull(userId)
            ?: return null

        val team = findTeamForCommandOrThrow(teamId)

        validateTeamMember(teamId, userId)

        val memberCount = teamMembershipRepository.countByTeamTeamIdAndLeftAtIsNull(team.teamId!!)

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
            teamPublicId = team.publicId,
            teamImageUrl = team.teamImageUrl,
            teamName = team.teamName,
            description = team.description,
            establishedOn = team.establishedOn,
            inviteCode = team.inviteCode,
            memberCount = memberCount,
            createdAt = DateTimeUtil.format(team.createdAt),
            updatedAt = DateTimeUtil.format(team.updatedAt),
            links = links,
            parts = parts,
            archiveCount = 0,
            cloudItemCount = 0
        )
    }

    @Transactional
    fun joinTeam(userId: Long, inviteCode: String?): TeamJoinResponse {
        val normalizedInviteCode = validateAndNormalizeInviteCode(inviteCode)
        userService.validateUserExists(userId)

        val team = findInviteCodeOrThrow(normalizedInviteCode)

        validateNotAlreadyJoined(team.teamId!!, userId)

        val teamMembership = TeamMemberships(
            team = team,
            userId = userId,
            teamRole = TeamRole.MEMBER
        )

        val savedMembership = teamMembershipRepository.save(teamMembership)

        //TODO: 하은아, 가입하면 currentTeamId를 해당 팀으로 바꿔야 하는지 언니들에게 물어봐

        return TeamJoinResponse(
            teamId = team.teamId!!,
            teamPublicId = team.publicId,
            teamName = team.teamName,
            teamRole = savedMembership.teamRole,
            joinedAt = DateTimeUtil.format(savedMembership.joinedAt),
        )
    }

    @Transactional(readOnly = true)
    fun getUserTeams(userId: Long) : UserTeamListResponse {
        userService.validateUserExists(userId)

        val memberships = teamMembershipRepository.findAllByUserIdAndLeftAtIsNullAndTeamDeletedAtIsNullOrderByJoinedAtDesc(userId)

        val teams = memberships.map { membership ->
            val team = membership.team
            TeamSimpleInfo(
                teamId = team.teamId!!,
                teamPublicId = team.publicId,
                teamName = team.teamName,
                teamType = team.teamType,
                teamImageUrl = team.teamImageUrl,
                createdAt =  DateTimeUtil.format(team.createdAt)
            )
        }

        return UserTeamListResponse(teams = teams)
    }

    @Transactional(readOnly = true)
    fun getTeamInfoByInviteCode(inviteCode: String): TeamSimpleInfo {
        val normalizedInviteCode = validateAndNormalizeInviteCode(inviteCode)

        val team = findInviteCodeOrThrow(normalizedInviteCode)

        return TeamSimpleInfo(
            teamId = team.teamId!!,
            teamPublicId = team.publicId,
            teamName = team.teamName,
            teamType = team.teamType,
            teamImageUrl = team.teamImageUrl,
            createdAt =  DateTimeUtil.format(team.createdAt)
        )
    }

    @Transactional
    fun selectTeam(userId: Long, teamPublicId: UUID) {
        userService.validateUserExists(userId)
        val team = findTeamForCommandOrThrow(teamPublicId)

        validateTeamMember(team.teamId!!, userId)

        userService.updateCurrentTeamId(userId, team.teamId!!)
    }

    private fun findInviteCodeOrThrow(inviteCode: String): Teams {
        return teamRepository.findByInviteCode(inviteCode)
            ?: throw BusinessException(ErrorCode.TEAM_INVITE_CODE_NOT_FOUND)
    }

    private fun findTeamForCommandOrThrow(teamId: Long): Teams {
        return teamRepository.findByTeamId(teamId)
            ?: throw BusinessException(ErrorCode.TEAM_UNAVAILABLE)
    }

    private fun findTeamForCommandOrThrow(teamPublicId: UUID): Teams {
        return teamRepository.findByPublicId(teamPublicId)
            ?: throw BusinessException(ErrorCode.TEAM_UNAVAILABLE)
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

        if ((request.teamName?.length ?: 0) > 100) {
            throw BusinessException(ErrorCode.TEAM_NAME_TOO_LONG)
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

    private fun findActiveMembershipOrThrow(teamId: Long, userId: Long): TeamMemberships {
        return teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(teamId, userId)
            ?: throw BusinessException(ErrorCode.TEAM_UNAVAILABLE)
    }

    private fun validateTeamRole(
        teamId: Long,
        userId: Long,
        roleErrorCode: ErrorCode,
        vararg allowedRoles: TeamRole
    ) {
        val membership = findActiveMembershipOrThrow(teamId, userId)

        if (membership.teamRole !in allowedRoles) {
            throw BusinessException(roleErrorCode)
        }
    }

    private fun validateTeamUpdatePermission(teamId: Long, userId: Long) {
        validateTeamRole(
            teamId = teamId,
            userId = userId,
            roleErrorCode = ErrorCode.TEAM_NO_UPDATE_PERMISSION,
            TeamRole.LEADER,
            TeamRole.MANAGER
        )
    }

    private fun validateTeamDeletePermission(teamId: Long, userId: Long) {
        validateTeamRole(
            teamId = teamId,
            userId = userId,
            roleErrorCode = ErrorCode.TEAM_NO_DELETE_PERMISSION,
            TeamRole.LEADER
        )
    }

    fun validateTeamMember(teamId: Long, userId: Long) {
        findActiveMembershipOrThrow(teamId, userId)
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
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(6)
            .uppercase()
    }

    fun validateMembersInTeam(teamId: Long, userIds: List<Long>): Boolean {
        if (userIds.isEmpty()) return false
        val activeMembers = teamMembershipRepository.findAllByTeamTeamIdAndUserIdInAndLeftAtIsNull(teamId, userIds)
        val uniqueUserIdsCount = userIds.distinct().size
        return activeMembers.size == uniqueUserIdsCount
    }
}