package com.beat_it.team.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.*
import com.beat_it.team.entity.Archives
import com.beat_it.team.entity.Teams
import com.beat_it.team.repository.ArchiveCommentsRepository
import com.beat_it.team.repository.ArchiveReactionsRepository
import com.beat_it.team.repository.ArchiveRepository
import com.beat_it.team.repository.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArchiveService(
    private val archiveRepository: ArchiveRepository,
    private val archiveCommentsRepository: ArchiveCommentsRepository,
    private val archiveReactionsRepository: ArchiveReactionsRepository,
    private val userService: UserService,
    private val teamService: TeamService,
) {

    @Transactional
    fun createArchive(userId: Long, request: ArchiveCreateRequest): ArchiveCreateResponse {
        validateCreateRequest(request)

        val team = findCurrentTeamForArchiveOrThrow(userId)

        val archive = Archives(
            writerId = userId,
            team = team,
            title = request.title,
            placeName = request.placeName,
            locationId = request.locationId,
            description = request.description,
            archiveImageUrl = request.archiveImageUrl,
            likeCount = 0,
            dislikeCount = 0,
            commentCount = 0,
        )

        val savedArchive = archiveRepository.save(archive)

        return ArchiveCreateResponse(
            archiveId = savedArchive.archiveId!!,
            title = savedArchive.title,
            placeName = savedArchive.placeName,
            locationId = savedArchive.locationId,
            createdAt = DateTimeUtil.format(savedArchive.createdAt),
        )
    }

    @Transactional
    fun getArchiveDetail(userId: Long, archiveId: Long): ArchiveDetailResponse {
        val team = findCurrentTeamForArchiveOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(team, archive)

        return ArchiveDetailResponse(
            archiveId = archive.archiveId!!,
            title = archive.title,
            placeName = archive.placeName,
            description = archive.description,
            archiveImageUrl = archive.archiveImageUrl,
            //TODO: 장소 LocaitonResponse도 추가해야 함.
            likeCount = archive.likeCount,
            dislikeCount = archive.dislikeCount,
            commentCount = archive.commentCount,
            createdAt = DateTimeUtil.format(archive.createdAt),
            updatedAt = DateTimeUtil.format(archive.updatedAt),
        )
    }

    @Transactional
    fun updateArchive(userId: Long, archiveId: Long, request: ArchiveUpdateRequest): ArchiveUpdateResponse {
        validateUpdateRequest(request)

        val team = findCurrentTeamForArchiveOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(team, archive)
        validateArchiveUpdatePermission(userId, archive)
        validateArchiveChanged(archive, request)

        archive.updateArchive(
            title = request.title,
            description = request.description,
            placeName = request.placeName,
            locationId = request.locationId,
            archiveImageUrl = request.archiveImageUrl,
        )

        return ArchiveUpdateResponse(
            archiveId = archive.archiveId!!,
            title = archive.title,
            description = archive.description,
            placeName = archive.placeName,
            locationId = archive.locationId,
            archiveImageUrl = archive.archiveImageUrl,
            updatedAt = DateTimeUtil.format(archive.updatedAt),
        )
    }

    @Transactional
    fun deleteArchive(userId: Long, archiveId: Long) {
        val team = findCurrentTeamForArchiveOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(team, archive)
        validateArchiveDeletePermission(userId, archive)

        archiveCommentsRepository.deleteByArchiveArchiveId(archiveId)
        archiveReactionsRepository.deleteByArchiveArchiveId(archiveId)

        archiveRepository.delete(archive)
    }

    private fun findArchiveOrThrow(archiveId: Long): Archives {
        return archiveRepository.findByArchiveId(archiveId)
            ?: throw BusinessException(ErrorCode.ARCHIVE_NOT_FOUND)
    }

    private fun findCurrentTeamForArchiveOrThrow(userId: Long) : Teams {
        userService.validateUserExists(userId)

        val teamId = userService.getCurrentTeamId(userId)
        val team = teamService.findTeamForCommandOrThrow(teamId)

        teamService.validateTeamMember(teamId, userId)

        return team
    }

    private fun validateArchiveBelongsToCurrentTeam(team: Teams, archive: Archives) {
        if (archive.team.teamId != team.teamId) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_PERMISSION)
        }
    }

    private fun validateArchiveUpdatePermission(userId: Long, archive: Archives) {
        if (archive.authorId != userId) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_UPDATE_PERMISSION)
        }
    }

    private fun validateArchiveDeletePermission(userId: Long, archive: Archives) {
        if (archive.authorId != userId) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_DELETE_PERMISSION)
        }
    }

    private fun validateCreateRequest(request: ArchiveCreateRequest) {
        if (request.title.isBlank()) {
            throw BusinessException(ErrorCode.ARCHIVE_TITLE_REQUIRED)
        }

        if (request.title.length > 100) {
            throw BusinessException(ErrorCode.ARCHIVE_TITLE_TOO_LONG)
        }

        if ((request.description?.length ?: 0) > 500) {
            throw BusinessException(ErrorCode.ARCHIVE_DESCRIPTION_TOO_LONG)
        }
    }

    private fun validateUpdateRequest(request: ArchiveUpdateRequest) {
        if (request.title != null && request.title.isBlank()) {
            throw BusinessException(ErrorCode.ARCHIVE_TITLE_REQUIRED)
        }

        if (request.title != null && request.title.length > 100) {
            throw BusinessException(ErrorCode.ARCHIVE_TITLE_TOO_LONG)
        }

        if ((request.description?.length ?: 0) > 500) {
            throw BusinessException(ErrorCode.ARCHIVE_DESCRIPTION_TOO_LONG)
        }
    }
    
    private fun validateArchiveChanged(
        archive: Archives,
        request: ArchiveUpdateRequest,
    ) {
        val isAnyFieldChanged =
            (request.title != null && request.title != archive.title) ||
                    (request.description != null && request.description != archive.description) ||
                    (request.placeName != null && request.placeName != archive.placeName) ||
                    (request.locationId != null && request.locationId != archive.locationId) ||
                    (request.archiveImageUrl != null && request.archiveImageUrl != archive.archiveImageUrl)

        if (!isAnyFieldChanged) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_CONTENT_TO_UPDATE)
        }
    }
}