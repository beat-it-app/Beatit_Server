package com.beat_it.team.service

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.location.service.LocationsService
import com.beat_it.post.dto.CommentResponse
import com.beat_it.team.dto.*
import com.beat_it.team.entity.ArchiveComments
import com.beat_it.team.entity.ArchiveRatings
import com.beat_it.team.entity.Archives
import com.beat_it.team.entity.ArchivesFiles
import com.beat_it.team.entity.Teams
import com.beat_it.team.repository.ArchiveCommentsRepository
import com.beat_it.team.repository.ArchiveRatingsRepository
import com.beat_it.team.repository.ArchiveRepository
import com.beat_it.team.repository.ArchivesFilesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ArchiveService(
    private val archiveRepository: ArchiveRepository,
    private val archiveCommentsRepository: ArchiveCommentsRepository,
    private val archiveRatingsRepository: ArchiveRatingsRepository,
    private val userService: UserService,
    private val teamService: TeamService,
    private val locationsService: LocationsService,
    private val archivesFilesRepository: ArchivesFilesRepository,
) {

    @Transactional
    fun createArchive(
        userId: Long,
        request: ArchiveCreateRequest,
        archiveImages: List<MultipartFile>?,
    ): ArchiveCreateResponse {
        validateTitle(request.title)
        validateDescription(request.description)

        val team = findCurrentTeamForArchiveOrThrow(userId)
        val locationId = request.locationId
            ?: throw BusinessException(ErrorCode.ARCHIVE_LOCATION_REQUIRED)
        val location = locationsService.getLocation(locationId)

        val archive = Archives(
            writerId = userId,
            team = team,
            title = request.title,
            roadAddress = location.roadAddress,
            locationId = location.locationId,
            description = request.description,
            archiveImageUrl = null,
            ratingSum = 0,
            ratingCount = 0,
            commentCount = 0,
        )

        val savedArchive = archiveRepository.save(archive)

        val savedArchiveFiles = saveArchiveImages(
            archive = savedArchive,
            userId = userId,
            archiveImages = archiveImages,
        )

        savedArchive.updateArchiveImageUrl(savedArchiveFiles.firstOrNull()?.cdnUrl)

        return ArchiveCreateResponse(
            archiveId = savedArchive.archiveId!!,
            teamId = team.teamId!!,
            writerId = savedArchive.writerId,
            title = savedArchive.title,
            roadAddress = savedArchive.roadAddress,
            locationId = savedArchive.locationId,
            archiveImageUrls = savedArchiveFiles.map { archiveFile -> archiveFile.cdnUrl },
            createdAt = DateTimeUtil.format(savedArchive.createdAt),
        )
    }

    @Transactional(readOnly = true)
    fun getTeamArchives(userId: Long): ArchiveListResponse {
        val team = findCurrentTeamForArchiveOrThrow(userId)
        val teamId = team.teamId!!

        val archives = archiveRepository
            .findAllByTeamTeamIdOrderByCreatedAtDesc(teamId)
            .map { archive ->
                ArchiveListItemResponse(
                    archiveId = archive.archiveId!!,
                    teamId = teamId,
                    writerId = archive.writerId,
                    title = archive.title,
                    roadAddress = archive.roadAddress,
                    locationId = archive.locationId,
                    archiveImageUrl = archive.archiveImageUrl,
                    averageRating = archive.calculateAverageRating(),
                    ratingCount = archive.ratingCount,
                    commentCount = archive.commentCount,
                    createdAt = DateTimeUtil.format(archive.createdAt),
                )
            }

        return ArchiveListResponse(archives = archives)
    }

    @Transactional(readOnly = true)
    fun getArchiveDetail(userId: Long, archiveId: Long): ArchiveDetailResponse {
        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        val writerProfile = userService.getUserProfile(archive.writerId)
        val archiveImageUrls = archivesFilesRepository
            .findAllByArchiveArchiveIdOrderByArchiveFileIdAsc(archiveId)
            .map { archiveFile -> archiveFile.cdnUrl }

        val myRating = archiveRatingsRepository
            .findByArchiveArchiveIdAndUserId(archiveId, userId)

        val comments = archiveCommentsRepository
            .findAllByArchiveArchiveIdOrderByCreatedAtAsc(archiveId)

        val commentResponses = toCommentResponses(
            comments = comments,
            archiveWriterId = archive.writerId,
            currentUserId = userId,
        )

        return ArchiveDetailResponse(
            archiveId = archive.archiveId!!,
            teamId = archive.team.teamId!!,
            writerId = archive.writerId,
            title = archive.title,
            roadAddress = archive.roadAddress,
            locationId = archive.locationId,
            description = archive.description,
            archiveImageUrls = archiveImageUrls,
            writerName = writerProfile?.name ?: "알 수 없음",
            writerProfileImageUrl = writerProfile?.authFile?.cdnUrl,
            isWriter = archive.writerId == userId,
            rating = ArchiveRatingResponse(
                averageRating = archive.calculateAverageRating(),
                ratingCount = archive.ratingCount,
                myRating = myRating?.score,
            ),
            commentCount = archive.commentCount,
            commentList = commentResponses,
            createdAt = DateTimeUtil.format(archive.createdAt),
            updatedAt = DateTimeUtil.format(archive.updatedAt),
        )
    }

    @Transactional
    fun updateArchive(
        userId: Long,
        archiveId: Long,
        request: ArchiveUpdateRequest,
        archiveImages: List<MultipartFile>?,
    ): ArchiveUpdateResponse {
        request.title?.let { validateTitle(it) }
        validateDescription(request.description)

        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        validateArchiveUpdatePermission(userId, archive)
        validateArchiveChanged(
            archive = archive,
            request = request,
            archiveImages = archiveImages,
        )

        val location = request.locationId?.let { locationId ->
            locationsService.getLocation(locationId)
        }

        archive.updateArchive(
            title = request.title,
            description = request.description,
            roadAddress = location?.roadAddress,
            locationId = location?.locationId,
        )

        updateArchiveImagesIfExists(
            archive = archive,
            userId = userId,
            archiveImages = archiveImages,
        )

        val archiveImageUrls = archivesFilesRepository
            .findAllByArchiveArchiveIdOrderByArchiveFileIdAsc(archiveId)
            .map { archiveFile -> archiveFile.cdnUrl }

        return ArchiveUpdateResponse(
            archiveId = archive.archiveId!!,
            title = archive.title,
            description = archive.description,
            roadAddress = archive.roadAddress,
            locationId = archive.locationId,
            archiveImageUrls = archiveImageUrls,
            updatedAt = DateTimeUtil.format(archive.updatedAt),
        )
    }

    @Transactional
    fun deleteArchive(userId: Long, archiveId: Long) {
        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        validateArchiveDeletePermission(userId, archive)

        archiveCommentsRepository.deleteByArchiveArchiveId(archiveId)
        archiveRatingsRepository.deleteByArchiveArchiveId(archiveId)
        archivesFilesRepository.deleteAllByArchiveArchiveId(archiveId)

        archiveRepository.delete(archive)
    }

    @Transactional
    fun saveRating(
        userId: Long,
        archiveId: Long,
        rating: Int,
    ): ArchiveRatingResponse {
        validateRating(rating)

        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        val existingRating = archiveRatingsRepository
            .findByArchiveArchiveIdAndUserId(archiveId, userId)

        if (existingRating == null) {
            archiveRatingsRepository.save(
                ArchiveRatings(
                    archive = archive,
                    userId = userId,
                    score = rating,
                )
            )
            archive.addRating(rating)
        } else {
            archive.updateRating(
                previousScore = existingRating.score,
                newScore = rating,
            )
            existingRating.updateScore(rating)
        }

        return ArchiveRatingResponse(
            averageRating = archive.calculateAverageRating(),
            ratingCount = archive.ratingCount,
            myRating = rating,
        )
    }

    @Transactional
    fun createComment(
        userId: Long,
        archiveId: Long,
        comment: String,
    ) {
        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        validateComment(comment)

        val comment = ArchiveComments.create(
            archive = archive,
            userId = userId,
            content = comment,
        )

        archiveCommentsRepository.save(comment)
        archive.increaseComment()
    }

    @Transactional
    fun deleteComment(
        userId: Long,
        archiveId: Long,
        commentId: Long,
    ) {
        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        val comment = archiveCommentsRepository
            .findByArchiveCommentIdAndArchiveArchiveId(commentId, archiveId)
            ?: throw BusinessException(ErrorCode.ARCHIVE_COMMENT_NOT_FOUND)

        validateCommentDeletePermission(
            comment = comment,
            userId = userId,
            archiveWriterId = archive.writerId,
        )

        archiveCommentsRepository.delete(comment)
        archive.decreaseComment()
    }

    private fun saveArchiveImages(
        archive: Archives,
        userId: Long,
        archiveImages: List<MultipartFile>?,
    ): List<ArchivesFiles> {
        val validImages = archiveImages
            .orEmpty()
            .filterNot { archiveImage -> archiveImage.isEmpty }

        val archiveFiles = if (validImages.isEmpty()) {
            listOf(
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
            )
        } else {
            validImages.mapIndexed { index, archiveImage ->
                // TODO : S3 연동 전 임시 처리. S3 붙으면 fileService.uploadFiles 로직으로 교체.
                ArchivesFiles(
                    archive = archive,
                    userId = userId,
                    originalFileName = archiveImage.originalFilename ?: "archive-image-${index + 1}.jpg",
                    storageKey = "dummy/path/archive-image-${index + 1}.jpg",
                    cdnUrl = "https://example.com/archive-image-${index + 1}.jpg",
                    mimeType = archiveImage.contentType,
                    mediaCategory = MediaCategory.IMAGE,
                    fileSizeBytes = archiveImage.size,
                    isPublic = true,
                )
            }
        }

        return archivesFilesRepository.saveAll(archiveFiles)
    }

    private fun updateArchiveImagesIfExists(
        archive: Archives,
        userId: Long,
        archiveImages: List<MultipartFile>?,
    ) {
        val validImages = archiveImages
            .orEmpty()
            .filterNot { archiveImage -> archiveImage.isEmpty }

        if (validImages.isEmpty()) {
            return
        }

        archivesFilesRepository.deleteAllByArchiveArchiveId(archive.archiveId!!)

        val savedArchiveFiles = saveArchiveImages(
            archive = archive,
            userId = userId,
            archiveImages = validImages,
        )

        archive.updateArchiveImageUrl(savedArchiveFiles.firstOrNull()?.cdnUrl)
    }

    private fun toCommentResponses(
        comments: List<ArchiveComments>,
        archiveWriterId: Long,
        currentUserId: Long,
    ): List<CommentResponse> {
        return comments.map { comment ->
            val writerProfile = userService.getUserProfile(comment.userId)

            CommentResponse(
                commentId = comment.archiveCommentId!!,
                writerName = writerProfile?.name ?: "알 수 없음",
                content = comment.content,
                createdAt = DateTimeUtil.format(comment.createdAt),
                profileImageUrl = writerProfile?.authFile?.cdnUrl,
                isWriter = comment.userId == archiveWriterId,
                isMine = comment.userId == currentUserId,
            )
        }
    }

    private fun findAccessibleArchiveOrThrow(
        userId: Long,
        archiveId: Long,
    ): Archives {
        val team = findCurrentTeamForArchiveOrThrow(userId)
        val archive = findArchiveOrThrow(archiveId)

        validateArchiveBelongsToCurrentTeam(team, archive)
        return archive
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

    private fun validateComment(content: String) {
        if (content.isBlank() || content.length > 1000) {
            throw BusinessException(ErrorCode.ARCHIVE_INVALID_COMMENT_CONTENT)
        }
    }

    private fun validateRating(score: Int) {
        if (score !in 1..5) {
            throw BusinessException(ErrorCode.ARCHIVE_INVALID_RATING)
        }
    }

    private fun validateCommentDeletePermission(
        comment: ArchiveComments,
        userId: Long,
        archiveWriterId: Long,
    ) {
        if (comment.userId != userId && archiveWriterId != userId) {
            throw BusinessException(ErrorCode.ARCHIVE_COMMENT_NO_DELETE_PERMISSION)
        }
    }

    private fun validateArchiveChanged(
        archive: Archives,
        request: ArchiveUpdateRequest,
        archiveImages: List<MultipartFile>?,
    ) {
        val isImageChanged = archiveImages
            .orEmpty()
            .any { archiveImage -> !archiveImage.isEmpty }

        val isAnyFieldChanged =
            (request.title != null && request.title != archive.title) ||
                    (request.description != null && request.description != archive.description) ||
                    (request.locationId != null && request.locationId != archive.locationId) ||
                    isImageChanged

        if (!isAnyFieldChanged) {
            throw BusinessException(ErrorCode.ARCHIVE_NO_CONTENT_TO_UPDATE)
        }
    }
}
