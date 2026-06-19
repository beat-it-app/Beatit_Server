package com.beat_it.team.service

import com.beat_it.auth.entity.Users
import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.dto.*
import com.beat_it.team.entity.ArchiveComments
import com.beat_it.team.entity.Archives
import com.beat_it.team.entity.TeamLinks
import com.beat_it.team.entity.TeamMemberships
import com.beat_it.team.entity.Teams
import com.beat_it.team.entity.enum.TeamRole
import com.beat_it.team.repository.ArchiveCommentsRepository
import com.beat_it.team.repository.ArchiveReactionsRepository
import com.beat_it.team.repository.ArchiveRepository
import com.beat_it.team.repository.TeamRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ArchiveService(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val archiveRepository: ArchiveRepository,
    private val archiveCommentsRepository: ArchiveCommentsRepository,
    private val archiveReactionsRepository: ArchiveReactionsRepository,
) {

    @Transactional
    fun createArchive(userId: Long, request: ArchiveCreateRequest): ArchiveCreateResponse {
        val user = findUserOrThrow(userId)

        val teamId = user.currentTeamId
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)
        val team = findTeamOrThrow(teamId)

        validateCreateRequest(request)

        val archive = Archives(
            authorId = user.userId!!,
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
            createdAt = savedArchive.createdAt,
        )
    }

    @Transactional
    fun getArchiveDetail(userId: Long, archiveId: Long): ArchiveDetailResponse {
        val user = findUserOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(user, archive)

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
            createdAt = archive.createdAt,
            updatedAt = archive.updatedAt,
        )
    }

    @Transactional
    fun updateArchive(userId: Long, archiveId: Long, request: ArchiveUpdateRequest): ArchiveUpdateResponse {
        val user = findUserOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(user, archive)
        validateArchiveUpdatePermission(user.userId!!, archive.archiveId!!)

        validateUpdateRequest(request)
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
            updatedAt = archive.updatedAt,
        )
    }

    @Transactional
    fun deleteArchive(userId: Long, archiveId: Long) {
        val user = findUserOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(user, archive)
        validateArchiveUpdatePermission(user.userId!!, archive.archiveId!!)

        archiveCommentsRepository.deleteByArchiveArchiveId(archiveId)
        archiveReactionsRepository.deleteByArchiveArchiveId(archiveId)

        archiveRepository.delete(archive)
    }


    private fun findUserOrThrow(userId: Long) : Users {
        return userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }


    //FIXME: 팀 검증에서는 TEAM에서만 가지고 오기
     fun findTeamOrThrow(teamId: Long): Teams {
        return teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)
    }

    private fun findArchiveOrThrow(archiveId: Long) : Archives {
        return archiveRepository.findByArchiveId(archiveId)
            ?: throw BusinessException()
    }

    private fun validateArchiveBelongsToCurrentTeam(user: Users, archive: Archives) {
        val currentTeamId = user.currentTeamId
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)

        if (archive.team.teamId != currentTeamId) {
            throw BusinessException(ErrorCode.TEAM_NOT_FOUND)
        }
    }

    private fun validateCreateRequest(request: ArchiveCreateRequest) {
        if (request.title.isBlank()) {
            throw BusinessException(ErrorCode.TEAM_NAME_REQUIRED)
        }

        if (request.title.length > 100) {
            throw BusinessException(ErrorCode.TEAM_NAME_TOO_LONG)
        }

        if ((request.description?.length ?: 0) > 500) {
            throw BusinessException(ErrorCode.TEAM_DESCRIPTION_TOO_LONG)
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
            throw BusinessException()
        }
    }


    private fun validateArchiveUpdatePermission(userId: Long, archiveId: Long) {
        // TODO: 팀 삭제 권한 검증
        val archive = archiveRepository.findByArchiveId(archiveId) ?: throw BusinessException()
        if (archive.authorId != userId) {
            throw BusinessException()
        }
    }

}