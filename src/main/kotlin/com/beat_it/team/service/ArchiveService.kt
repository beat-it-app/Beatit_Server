package com.beat_it.team.service

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.team.dto.archive.ArchiveCreateRequest
import com.beat_it.team.dto.archive.ArchiveCreateResponse
import com.beat_it.team.dto.archive.ArchiveDetailResponse
import com.beat_it.team.dto.archive.ArchiveSimpleInfo
import com.beat_it.team.dto.archive.ArchiveUpdateRequest
import com.beat_it.team.dto.archive.ArchiveUpdateResponse
import com.beat_it.team.dto.archive.TeamArchiveListResponse
import com.beat_it.team.entity.archive.Archives
import com.beat_it.team.entity.archive.ArchivesFiles
import com.beat_it.team.entity.Teams
import com.beat_it.team.repository.archive.ArchiveCommentsRepository
import com.beat_it.team.repository.archive.ArchiveReactionsRepository
import com.beat_it.team.repository.archive.ArchiveRepository
import com.beat_it.team.repository.archive.ArchivesFilesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ArchiveService(
    private val archiveRepository: ArchiveRepository,
    private val archiveCommentsRepository: ArchiveCommentsRepository,
    private val archiveReactionsRepository: ArchiveReactionsRepository,
    private val userService: UserService,
    private val teamService: TeamService,
    private val fileService: FileService,
    private val archivesFilesRepository: ArchivesFilesRepository,
) {

    @Transactional
    fun createArchive(
        userId: Long,
        request: ArchiveCreateRequest,
        archiveImage: MultipartFile?,
    ): ArchiveCreateResponse {
        validateTitle(request.title)
        validateDescription(request.description)

        val team = findCurrentTeamForArchiveOrThrow(userId)

        val archive = Archives(
            writerId = userId,
            team = team,
            title = request.title,
            placeName = request.placeName,
            locationId = request.locationId,
            description = request.description,
            archiveImageUrl = null,
            likeCount = 0,
            dislikeCount = 0,
            commentCount = 0,
        )

        val savedArchive = archiveRepository.save(archive)

        val savedArchiveFile = saveArchiveImage(
            archive = savedArchive,
            userId = userId,
            archiveImage = archiveImage,
        )

        savedArchive.updateArchiveImageUrl(savedArchiveFile.cdnUrl)

        return ArchiveCreateResponse(
            archiveId = savedArchive.archiveId!!,
            title = savedArchive.title,
            placeName = savedArchive.placeName,
            locationId = savedArchive.locationId,
            archiveImageUrl = savedArchive.archiveImageUrl,
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
    fun updateArchive(
        userId: Long,
        archiveId: Long,
        request: ArchiveUpdateRequest,
        archiveImage: MultipartFile?,
    ): ArchiveUpdateResponse {
        request.title?.let { validateTitle(it) }
        validateDescription(request.description)

        val team = findCurrentTeamForArchiveOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(team, archive)
        validateArchiveUpdatePermission(userId, archive)
        validateArchiveChanged(
            archive = archive,
            request = request,
            archiveImage = archiveImage,
        )

        archive.updateArchive(
            title = request.title,
            description = request.description,
            placeName = request.placeName,
            locationId = request.locationId,
        )

        updateArchiveImageIfExists(
            archive = archive,
            userId = userId,
            archiveImage = archiveImage,
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
        archivesFilesRepository.deleteAllByArchiveArchiveId(archiveId)

        archiveRepository.delete(archive)
    }

    @Transactional(readOnly = true)
    fun getTeamArchives(userId: Long): TeamArchiveListResponse {
        val team = findCurrentTeamForArchiveOrThrow(userId)

        val archives = archiveRepository
            .findAllByTeamTeamIdOrderByCreatedAtDesc(team.teamId!!)
            .map { archive ->
                ArchiveSimpleInfo(
                    archiveId = archive.archiveId!!,
                    title = archive.title,
                    placeName = archive.placeName,
                    archiveImageUrl = archive.archiveImageUrl,
                    likeCount = archive.likeCount,
                    dislikeCount = archive.dislikeCount,
                    commentCount = archive.commentCount,
                    createdAt = DateTimeUtil.format(archive.createdAt),
                )
            }

        return TeamArchiveListResponse(
            archives = archives,
        )
    }

    private fun saveArchiveImage(
        archive: Archives,
        userId: Long,
        archiveImage: MultipartFile?,
    ): ArchivesFiles {
        val archiveFile = if (archiveImage != null && !archiveImage.isEmpty) {
            // TODO : S3 연동 전 임시 처리. S3 붙으면 fileService.uploadFiles 로직으로 교체.
            ArchivesFiles(
                archive = archive,
                userId = userId,
                originalFileName = archiveImage.originalFilename ?: "archive-image.jpg",
                storageKey = "dummy/path/archive-image.jpg",
                cdnUrl = "https://example.com/default-archive-image.jpg",
                mimeType = archiveImage.contentType,
                mediaCategory = MediaCategory.IMAGE,
                fileSizeBytes = archiveImage.size,
                isPublic = true,
            )
        } else {
            ArchivesFiles(
                archive = archive,
                userId = userId,
                originalFileName = "default-archive.jpg",
                storageKey = "dummy/path/default-archive.jpg",
                cdnUrl = "https://example.com/default-archive-image.jpg",
                mimeType = "image/jpeg",
                mediaCategory = MediaCategory.IMAGE,
                fileSizeBytes = 0L,
                isPublic = true,
            )
        }

        return archivesFilesRepository.save(archiveFile)
    }

    private fun updateArchiveImageIfExists(
        archive: Archives,
        userId: Long,
        archiveImage: MultipartFile?,
    ) {
        if (archiveImage == null || archiveImage.isEmpty) {
            return
        }

        archivesFilesRepository.deleteAllByArchiveArchiveId(archive.archiveId!!)

        val savedArchiveFile = saveArchiveImage(
            archive = archive,
            userId = userId,
            archiveImage = archiveImage,
        )

        archive.updateArchiveImageUrl(savedArchiveFile.cdnUrl)
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
        if (archive.writerId != userId) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_UPDATE_PERMISSION)
        }
    }

    private fun validateArchiveDeletePermission(userId: Long, archive: Archives) {
        if (archive.writerId != userId) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_DELETE_PERMISSION)
        }
    }

    private fun validateTitle(title: String) {
        if (title.isBlank()) {
            throw BusinessException(ErrorCode.ARCHIVE_TITLE_REQUIRED)
        }

        if (title.length > 100) {
            throw BusinessException(ErrorCode.ARCHIVE_TITLE_TOO_LONG)
        }
    }

    private fun validateDescription(description: String?) {
        if ((description?.length ?: 0) > 500) {
            throw BusinessException(ErrorCode.ARCHIVE_DESCRIPTION_TOO_LONG)
        }
    }

    private fun validateArchiveChanged(
        archive: Archives,
        request: ArchiveUpdateRequest,
        archiveImage: MultipartFile?,
    ) {
        val isImageChanged = archiveImage != null && !archiveImage.isEmpty

        val isAnyFieldChanged =
            (request.title != null && request.title != archive.title) ||
                    (request.description != null && request.description != archive.description) ||
                    (request.placeName != null && request.placeName != archive.placeName) ||
                    (request.locationId != null && request.locationId != archive.locationId) ||
                    isImageChanged

        if (!isAnyFieldChanged) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_CONTENT_TO_UPDATE)
        }
    }
}