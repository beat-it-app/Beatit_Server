package com.beat_it.team.service

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.location.service.LocationsService
import com.beat_it.team.dto.*
import com.beat_it.team.entity.ArchiveCommentMentions
import com.beat_it.team.entity.ArchiveComments
import com.beat_it.team.entity.ArchiveRatings
import com.beat_it.team.entity.Archives
import com.beat_it.team.entity.ArchivesFiles
import com.beat_it.team.entity.Teams
import com.beat_it.team.repository.ArchiveCommentMentionRepository
import com.beat_it.team.repository.ArchiveCommentsRepository
import com.beat_it.team.repository.ArchiveRatingsRepository
import com.beat_it.team.repository.ArchiveRepository
import com.beat_it.team.repository.ArchivesFilesRepository
import com.beat_it.team.repository.TeamMembershipRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ArchiveService(
    private val archiveRepository: ArchiveRepository,
    private val archiveCommentsRepository: ArchiveCommentsRepository,
    private val archiveCommentMentionRepository: ArchiveCommentMentionRepository,
    private val archiveRatingsRepository: ArchiveRatingsRepository,
    private val userService: UserService,
    private val teamService: TeamService,
    private val locationsService: LocationsService,
    private val archivesFilesRepository: ArchivesFilesRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
) {

    private val mentionRegex = Regex("""@\{([^}]+)\}|@([a-zA-Z0-9가-힣_]+)""")

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
            averageRating = 0.0,
            ratingCount = 0,
            commentCount = 0,
            topArchive = false,
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
            createdAt = savedArchive.createdAt,
        )
    }

    @Transactional(readOnly = true)
    fun getTeamArchives(
        userId: Long,
        sort: String = "LATEST",
        page: Int = 0,
        size: Int = 10,
    ): ArchiveListResponse {
        val team = findCurrentTeamForArchiveOrThrow(userId)
        val teamId = team.teamId!!

        val archivesPage = when (sort.uppercase()) {
            "LATEST" -> {
                val pageRequest = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "archiveId")),
                )
                archiveRepository.findAllByTeamTeamId(teamId, pageRequest)
            }

            "RATING_DESC" -> {
                archiveRepository.findAllByTeamTeamIdOrderByRatingDesc(
                    teamId = teamId,
                    pageable = PageRequest.of(page, size),
                )
            }

            "RATING_ASC" -> {
                archiveRepository.findAllByTeamTeamIdOrderByRatingAsc(
                    teamId = teamId,
                    pageable = PageRequest.of(page, size),
                )
            }

            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val archives = archivesPage.content.map { archive ->
            archive.toListItemResponse(teamId)
        }

        return ArchiveListResponse(
            archives = archives,
            totalCount = archivesPage.totalElements.toInt(),
            hasNext = archivesPage.hasNext(),
        )
    }

    private fun Archives.toListItemResponse(teamId: Long): ArchiveListItemResponse {
        return ArchiveListItemResponse(
            archiveId = archiveId!!,
            teamId = teamId,
            writerId = writerId,
            title = title,
            roadAddress = roadAddress,
            archiveImageUrl = archiveImageUrl,
            averageRating = roundedAverageRating(),
            commentCount = commentCount,
        )
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
            topArchive = archive.topArchive,
            rating = ArchiveRatingResponse(
                averageRating = archive.roundedAverageRating(),
                ratingCount = archive.ratingCount,
                myRating = myRating?.score,
            ),
            commentCount = archive.commentCount,
            commentList = commentResponses,
            createdAt = archive.createdAt,
            updatedAt = archive.updatedAt,
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
            updatedAt = archive.updatedAt,
        )
    }

    @Transactional
    fun deleteArchive(userId: Long, archiveId: Long) {
        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        validateArchiveDeletePermission(userId, archive)
        val teamId = archive.team.teamId!!

        deleteCommentsByArchive(archiveId)
        archiveRatingsRepository.deleteByArchiveArchiveId(archiveId)
        archivesFilesRepository.deleteAllByArchiveArchiveId(archiveId)

        archiveRepository.delete(archive)
        archiveRepository.flush()
        refreshTopArchives(teamId)
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
            archiveRatingsRepository.saveAndFlush(
                ArchiveRatings(
                    archive = archive,
                    userId = userId,
                    score = rating,
                )
            )
            archive.increaseRatingCount()
        } else {
            existingRating.updateScore(rating)
            archiveRatingsRepository.saveAndFlush(existingRating)
        }

        val averageRating = archiveRatingsRepository
            .findAverageScoreByArchiveId(archiveId)
            ?: 0.0

        archive.updateAverageRating(averageRating)
        refreshTopArchives(archive.team.teamId!!)

        return ArchiveRatingResponse(
            averageRating = archive.roundedAverageRating(),
            ratingCount = archive.ratingCount,
            myRating = rating,
        )
    }

    @Transactional
    fun createComment(
        userId: Long,
        archiveId: Long,
        comment: String,
        parentCommentId: Long? = null,
        mentionedUserIds: List<Long>? = null,
    ) {
        val archive = findAccessibleArchiveOrThrow(userId, archiveId)
        validateComment(comment)

        var effectiveParentId: Long? = null

        parentCommentId?.let { requestedParentId ->
            val parentComment = archiveCommentsRepository
                .findByArchiveCommentIdAndArchiveArchiveId(requestedParentId, archiveId)
                ?: throw BusinessException(ErrorCode.ARCHIVE_COMMENT_NOT_FOUND)

            effectiveParentId = parentComment.parentCommentId ?: parentComment.archiveCommentId
        }

        val archiveComment = ArchiveComments.create(
            archive = archive,
            userId = userId,
            content = comment,
            parentCommentId = effectiveParentId,
        )

        val savedComment = archiveCommentsRepository.save(archiveComment)
        saveMentions(
            comment = savedComment,
            teamId = archive.team.teamId!!,
            content = comment,
            explicitMentionedUserIds = mentionedUserIds,
        )
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

        var totalDeletedCount = 1

        if (comment.parentCommentId == null) {
            val childReplies = archiveCommentsRepository.findByParentCommentId(commentId)
            if (childReplies.isNotEmpty()) {
                val childReplyIds = childReplies.mapNotNull { reply -> reply.archiveCommentId }
                if (childReplyIds.isNotEmpty()) {
                    archiveCommentMentionRepository.deleteByCommentArchiveCommentIdIn(childReplyIds)
                }
                archiveCommentsRepository.deleteAll(childReplies)
                totalDeletedCount += childReplies.size
            }
        }

        archiveCommentMentionRepository.deleteByComment(comment)
        archiveCommentsRepository.delete(comment)
        archive.decreaseComment(totalDeletedCount)
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
    ): List<ArchiveCommentResponse> {
        if (comments.isEmpty()) {
            return emptyList()
        }

        val commentUserIds = comments.map { comment -> comment.userId }
        val mentions = archiveCommentMentionRepository.findByCommentIn(comments)
        val mentionsByCommentId = mentions.groupBy { mention -> mention.comment.archiveCommentId }
        val mentionedUserIds = mentions.map { mention -> mention.mentionedUserId }

        val allUserIds = (commentUserIds + mentionedUserIds).distinct()
        val userProfilesMap = userService.getUserProfiles(allUserIds)
            .associateBy { profile -> profile.userId }

        val allCommentResponses = comments.map { comment ->
            val writerProfile = userProfilesMap[comment.userId]
            val commentMentions = mentionsByCommentId[comment.archiveCommentId] ?: emptyList()
            val mentionedUserResponses = commentMentions.map { mention ->
                val mentionedProfile = userProfilesMap[mention.mentionedUserId]
                ArchiveMentionUserResponse(
                    userId = mention.mentionedUserId,
                    name = mentionedProfile?.name ?: mention.mentionedName,
                    profileImageUrl = mentionedProfile?.profileImageUrl,
                )
            }

            ArchiveCommentResponse(
                commentId = comment.archiveCommentId!!,
                parentCommentId = comment.parentCommentId,
                writerName = writerProfile?.name ?: "알 수 없음",
                content = comment.content,
                createdAt = comment.createdAt,
                profileImageUrl = writerProfile?.profileImageUrl,
                isWriter = comment.userId == archiveWriterId,
                isMine = comment.userId == currentUserId,
                mentionedUsers = mentionedUserResponses,
                replies = emptyList(),
            )
        }

        val (rootComments, replyComments) = allCommentResponses.partition { response ->
            response.parentCommentId == null
        }
        val repliesByParentId = replyComments.groupBy { response -> response.parentCommentId }

        return rootComments.map { root ->
            root.copy(replies = repliesByParentId[root.commentId] ?: emptyList())
        }
    }

    private fun saveMentions(
        comment: ArchiveComments,
        teamId: Long,
        content: String,
        explicitMentionedUserIds: List<Long>?,
    ) {
        val activeMembers = teamMembershipRepository.findAllByTeamTeamIdAndLeftAtIsNull(teamId)
        if (activeMembers.isEmpty()) {
            return
        }

        val activeMemberUserIds = activeMembers.map { membership -> membership.userId }.toSet()
        val memberProfiles = userService.getUserProfiles(activeMemberUserIds.toList())
        val profileByName = memberProfiles.associateBy { profile -> profile.name }
        val profileById = memberProfiles.associateBy { profile -> profile.userId }

        val mentionsToSave = mutableListOf<ArchiveCommentMentions>()
        val alreadyMentionedUserIds = mutableSetOf<Long>()

        explicitMentionedUserIds?.distinct()?.forEach { mentionedUserId ->
            if (mentionedUserId in activeMemberUserIds) {
                profileById[mentionedUserId]?.let { profile ->
                    mentionsToSave.add(
                        ArchiveCommentMentions.create(
                            comment = comment,
                            mentionedUserId = profile.userId,
                            mentionedName = profile.name,
                        )
                    )
                    alreadyMentionedUserIds.add(profile.userId)
                }
            }
        }

        extractMentionNames(content).forEach { name ->
            profileByName[name]?.let { profile ->
                if (profile.userId !in alreadyMentionedUserIds) {
                    mentionsToSave.add(
                        ArchiveCommentMentions.create(
                            comment = comment,
                            mentionedUserId = profile.userId,
                            mentionedName = profile.name,
                        )
                    )
                    alreadyMentionedUserIds.add(profile.userId)
                }
            }
        }

        if (mentionsToSave.isNotEmpty()) {
            archiveCommentMentionRepository.saveAll(mentionsToSave)
        }
    }

    private fun extractMentionNames(content: String): List<String> {
        return mentionRegex.findAll(content)
            .mapNotNull { match ->
                (match.groups[1]?.value ?: match.groups[2]?.value)
                    ?.trim()
                    ?.takeIf { name -> name.isNotBlank() }
            }
            .distinct()
            .toList()
    }

    private fun deleteCommentsByArchive(archiveId: Long) {
        val comments = archiveCommentsRepository.findAllByArchiveArchiveIdOrderByCreatedAtAsc(archiveId)
        if (comments.isEmpty()) {
            return
        }

        val commentIds = comments.mapNotNull { comment -> comment.archiveCommentId }
        if (commentIds.isNotEmpty()) {
            archiveCommentMentionRepository.deleteByCommentArchiveCommentIdIn(commentIds)
        }
        archiveCommentsRepository.deleteByArchiveArchiveId(archiveId)
    }

    private fun refreshTopArchives(teamId: Long) {
        val archives = archiveRepository.findAllByTeamTeamId(teamId)
        val maxAverageRating = archives
            .filter { archive -> archive.ratingCount > 0 }
            .maxOfOrNull { archive -> archive.averageRating }

        archives.forEach { archive ->
            archive.updateTopArchive(
                topArchive = maxAverageRating != null &&
                        archive.ratingCount > 0 &&
                        archive.averageRating == maxAverageRating,
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

    private fun findCurrentTeamForArchiveOrThrow(userId: Long): Teams {
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
