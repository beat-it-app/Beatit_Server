package com.beat_it.post.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.post.dto.*
import com.beat_it.post.entity.notice.NoticeAttachments
import com.beat_it.post.entity.notice.Notices
import com.beat_it.post.entity.enum.FileType
import com.beat_it.post.entity.enum.PostType
import com.beat_it.post.entity.enum.ReactionType
import com.beat_it.post.repository.*
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import com.beat_it.post.entity.PostFiles
import com.beat_it.auth.service.UserService
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.global.service.FileService
import com.beat_it.post.dto.notice.NoticeDetailResponse
import com.beat_it.post.dto.notice.NoticeItems
import com.beat_it.post.dto.notice.NoticeListResponse
import com.beat_it.post.dto.notice.NoticeReactionDto
import com.beat_it.post.dto.notice.NoticeRequest
import com.beat_it.post.entity.notice.NoticeReactions
import com.beat_it.post.entity.PostComments
import com.beat_it.post.entity.enum.NoticeSortType
import com.beat_it.post.repository.notice.NoticeAttachmentsRepository
import com.beat_it.post.repository.notice.NoticeReactionRepository
import com.beat_it.notification.event.NotificationEvent
import com.beat_it.notification.template.NotificationTemplate
import com.beat_it.post.repository.notice.NoticeRepository
import com.beat_it.team.service.TeamService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val fileService: FileService,
    private val noticeAttachmentsRepository: NoticeAttachmentsRepository,
    private val noticeReactionRepository: NoticeReactionRepository,
    private val commentService: CommentService,
    private val postFilesRepository: PostFilesRepository,
    private val userService: UserService,
    private val teamService: TeamService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional(readOnly = true)
    fun getNoticeList(
        userId: Long,
        keyword: String?,
        sort: NoticeSortType,
        page: Int = 0,
        size: Int = 10
    ): NoticeListResponse {
        val teamId = userService.getCurrentTeamId(userId)

        val sortObj = when (sort) {
            NoticeSortType.OLDEST -> Sort.by(Sort.Direction.ASC, "createdAt")
            NoticeSortType.LATEST -> Sort.by(Sort.Direction.DESC, "createdAt")
        }

        val pageRequest = PageRequest.of(page, size, sortObj)

        val searchKeyword = keyword ?: ""
        val noticesPage = noticeRepository.searchNotices(teamId, searchKeyword, pageRequest)
        val notices = noticesPage.content

        if (notices.isEmpty()) {
            return NoticeListResponse(
                noticeListResponse = emptyList(),
                totalCount = noticesPage.totalElements.toInt(),
                hasNext = noticesPage.hasNext()
            )
        }

        val userIds = notices.map { it.userId }.distinct()
        val userProfilesMap = userService.getUserProfiles(userIds).associateBy { it.userId }

        val noticeItems = notices.map { notice ->
            val userProfile = userProfilesMap[notice.userId]
            val writerName = userProfile?.name ?: "알 수 없음"

            val description = if (notice.content.length > 20) {
                "${notice.content.take(20)}..."
            } else {
                notice.content
            }

            NoticeItems(
                noticeId = notice.noticeId!!,
                title = notice.title,
                description = description,
                likeCount = notice.likeCounter,
                dislikeCount = notice.dislikeCounter,
                commentCount = notice.commentCounter,
                createdAt = notice.createdAt,
                writer = writerName,
                thumbnailUrl = notice.thumbnailImageUrl
            )
        }

        return NoticeListResponse(
            noticeListResponse = noticeItems,
            totalCount = noticesPage.totalElements.toInt(),
            hasNext = noticesPage.hasNext()
        )
    }

    @Transactional
    fun createNotice(userId: Long, dto: NoticeRequest, images: List<MultipartFile>?) {
        val teamId = userService.getCurrentTeamId(userId)
        userService.getCurrentTeamId(userId)
        validateTitleAndContent(dto.title, dto.content)

        val validImages = images.orEmpty().filterNot { it.isEmpty }
        if (validImages.size > 10) {
            throw BusinessException(ErrorCode.NOTICE_IMAGE_EXCEEDED)
        }

        val uploadedPostFiles = uploadAndSavePostFiles(userId, validImages)
        val thumbnailUrl = uploadedPostFiles.firstOrNull()?.cdnUrl

        val notice = Notices.writeNotice(
            userId = userId,
            teamId = teamId,
            title = dto.title,
            content = dto.content,
            thumbnailImageUrl = thumbnailUrl
        )

        val savedNotice = noticeRepository.save(notice)
        saveNoticeAttachments(savedNotice, uploadedPostFiles, userId)

        val teamMembers = teamService.getTeamMemberUserIds(teamId)
        val targetUserIds = teamMembers.filter { it != userId }
        if (targetUserIds.isNotEmpty()) {
            val teamName = teamService.getTeamName(teamId)
            val authorName = userService.getUserProfile(userId)?.name ?: "공지 작성자"
            eventPublisher.publishEvent(
                NotificationEvent(
                    targetUserIds = targetUserIds,
                    teamId = teamId,
                    message = NotificationTemplate.noticeRegistered(
                        teamName = teamName,
                        noticeTitle = savedNotice.title,
                        authorName = authorName,
                        noticeId = savedNotice.noticeId
                    )
                )
            )
        }
    }

    @Transactional(readOnly = true)
    fun getNotice(userId: Long, noticeId: Long): NoticeDetailResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val notice = getNotice(noticeId)
        validateTeam(notice, teamId)

        val writerProfile = userService.getUserProfileResponse(notice.userId)
        val writerName = writerProfile?.name ?: "알 수 없음"

        val attachments = noticeAttachmentsRepository.findByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId)
        val imageUrls = attachments.map { it.postFile.cdnUrl }

        val userReaction = noticeReactionRepository.findByNoticeNoticeIdAndUserId(noticeId, userId)
        val isLiked = userReaction.map { it.reactionType == ReactionType.LIKE }.orElse(false)
        val isDisliked = userReaction.map { it.reactionType == ReactionType.DISLIKE }.orElse(false)

        val comments = commentService.getComments(
            postType = PostType.NOTICE,
            postId = noticeId,
            postWriterId = notice.userId,
            currentUserId = userId
        )

        val reactionDto = NoticeReactionDto(
            likeCount = notice.likeCounter,
            dislikeCount = notice.dislikeCounter,
            isLiked = isLiked!!,
            isDisliked = isDisliked!!,
            commentCount = comments.sumOf { 1 + it.replies.size }
        )

        return NoticeDetailResponse(
            noticeId = notice.noticeId!!,
            title = notice.title,
            content = notice.content,
            writerName = writerName,
            writerProfileImageUrl = writerProfile?.profileImageUrl,
            createdAt = notice.createdAt,
            updatedAt = notice.updatedAt,
            images = imageUrls,
            isWriter = notice.userId == userId,
            reaction = reactionDto,
            commentList = comments
        )
    }

    @Transactional
    fun editNotice(
        userId: Long,
        noticeId: Long,
        dto: NoticeRequest,
        images: List<MultipartFile>?
    ) {
        validateTitleAndContent(dto.title, dto.content)
        val teamId = userService.getCurrentTeamId(userId)

        val notice = getNotice(noticeId)
        validateTeam(notice, teamId)
        validateWriter(notice, userId)

        val existingAttachments = noticeAttachmentsRepository.findByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId)
        val retainList = dto.retainNoticeImageUrls
        val retainUrls = retainList?.toSet()

        if (retainList != null &&
            (retainList.distinct().size != retainList.size ||
                !existingAttachments.map { it.postFile.cdnUrl }.toSet().containsAll(retainList))) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val validImages = images.orEmpty().filterNot { it.isEmpty }
        val retainedCount = if (retainUrls != null) {
            existingAttachments.count { it.postFile.cdnUrl in retainUrls }
        } else {
            if (validImages.isNotEmpty()) 0 else existingAttachments.size
        }
        if (retainedCount + validImages.size > 10) {
            throw BusinessException(ErrorCode.NOTICE_IMAGE_EXCEEDED)
        }

        val hasNewImages = validImages.isNotEmpty()
        val isImageChanged = if (retainUrls != null) {
            hasNewImages || retainUrls != existingAttachments.map { it.postFile.cdnUrl }.toSet()
        } else {
            images != null && (hasNewImages || existingAttachments.isNotEmpty())
        }

        val isAnyFieldChanged = (dto.title != notice.title) ||
                (dto.content != notice.content) ||
                isImageChanged

        if (!isAnyFieldChanged) {
            throw BusinessException(ErrorCode.POST_NO_CONTENT_TO_UPDATE)
        }

        var thumbnailUrl = notice.thumbnailImageUrl

        if (retainUrls != null) {
            val retainedAttachments = existingAttachments.filter { it.postFile.cdnUrl in retainUrls }
            val attachmentsToRemove = existingAttachments.filterNot { it.postFile.cdnUrl in retainUrls }

            if (attachmentsToRemove.isNotEmpty()) {
                deleteNoticeAttachments(attachmentsToRemove)
            }

            retainedAttachments.forEachIndexed { index, att ->
                if (att.displayOrder != index) {
                    att.displayOrder = index
                    noticeAttachmentsRepository.save(att)
                }
            }

            val uploadedPostFiles = uploadAndSavePostFiles(userId, validImages)
            val startIndex = retainedAttachments.size
            if (uploadedPostFiles.isNotEmpty()) {
                val newAttachments = uploadedPostFiles.mapIndexed { index, postFile ->
                    NoticeAttachments(
                        notice = notice,
                        postFile = postFile,
                        userId = userId,
                        fileType = FileType.IMAGE,
                        displayOrder = startIndex + index
                    )
                }
                noticeAttachmentsRepository.saveAll(newAttachments)
            }

            thumbnailUrl = (retainedAttachments.map { it.postFile.cdnUrl } + uploadedPostFiles.map { it.cdnUrl }).firstOrNull()
        } else if (images != null) {
            deleteNoticeAttachments(existingAttachments)

            val uploadedPostFiles = uploadAndSavePostFiles(userId, validImages)
            thumbnailUrl = if (uploadedPostFiles.isNotEmpty()) {
                uploadedPostFiles.first().cdnUrl
            } else {
                null
            }
            saveNoticeAttachments(notice, uploadedPostFiles, userId)
        }

        notice.update(
            title = dto.title,
            content = dto.content,
            thumbnailImageUrl = thumbnailUrl
        )
        noticeRepository.save(notice)
    }

    @Transactional
    fun deleteNotice(userId: Long, noticeId: Long) {
        val teamId = userService.getCurrentTeamId(userId)
        val notice = getNotice(noticeId)
        validateTeam(notice, teamId)
        validateWriter(notice, userId)

        val attachments = noticeAttachmentsRepository.findByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId)
        deleteNoticeAttachments(attachments)

        noticeReactionRepository.deleteByNoticeNoticeId(noticeId)
        commentService.deleteCommentsByPost(PostType.NOTICE, noticeId)
        noticeRepository.delete(notice)
    }

    private fun uploadAndSavePostFiles(
        userId: Long,
        images: List<MultipartFile>?
    ): List<PostFiles> {
        val validImages = images?.filter { !it.isEmpty } ?: emptyList()
        val uploadedFiles = if (validImages.isNotEmpty()) {
            fileService.uploadFiles(validImages, com.beat_it.global.service.FileDirectory.NOTICE)
        } else {
            emptyList()
        }

        return uploadedFiles.map { result ->
            val postFile = PostFiles(
                userId = userId,
                originalFileName = result.originalFileName,
                storageKey = result.storageKey,
                cdnUrl = result.cdnUrl,
                mediaCategory = MediaCategory.IMAGE,
                isPublic = true
            )
            postFilesRepository.save(postFile)
        }
    }

    private fun saveNoticeAttachments(notice: Notices, postFiles: List<PostFiles>, userId: Long) {
        if (postFiles.isEmpty()) return

        val attachments = postFiles.mapIndexed { index, postFile ->
            NoticeAttachments(
                notice = notice,
                postFile = postFile,
                userId = userId,
                fileType = FileType.IMAGE,
                displayOrder = index
            )
        }
        noticeAttachmentsRepository.saveAll(attachments)
    }

    private fun deleteNoticeAttachments(attachments: List<NoticeAttachments>) {
        if (attachments.isEmpty()) return
        val storageKeys = attachments.map { it.postFile.storageKey }.filter { it.isNotBlank() }
        if (storageKeys.isNotEmpty()) {
            fileService.deleteFiles(storageKeys)
        }

        attachments.forEach { attachment ->
            attachment.postFile.delete()
            postFilesRepository.save(attachment.postFile)
        }
        noticeAttachmentsRepository.deleteAll(attachments)
    }

    @Transactional
    fun toggleLike(userId: Long, noticeId: Long): Boolean {
        val notice = getNotice(noticeId)
        userService.getCurrentTeamId(userId)

        val existingReaction = noticeReactionRepository.findByNoticeNoticeIdAndUserId(noticeId, userId)

        if (existingReaction.isPresent) {
            val reaction = existingReaction.get()
            if (reaction.reactionType == ReactionType.LIKE) {
                noticeReactionRepository.delete(reaction)
                notice.decreaseLike()
                noticeRepository.save(notice)
                return false
            } else {
                throw BusinessException(ErrorCode.ALREADY_DISLIKED)
            }
        } else {
            val newReaction = NoticeReactions(
                notice = notice,
                userId = userId,
                reactionType = ReactionType.LIKE
            )
            noticeReactionRepository.save(newReaction)
            notice.increaseLike()
            noticeRepository.save(notice)
            return true
        }
    }

    @Transactional
    fun toggleDislike(userId: Long, noticeId: Long): Boolean {
        val notice = getNotice(noticeId)
        userService.getCurrentTeamId(userId)

        val existingReaction = noticeReactionRepository.findByNoticeNoticeIdAndUserId(noticeId, userId)

        if (existingReaction.isPresent) {
            val reaction = existingReaction.get()
            if (reaction.reactionType == ReactionType.DISLIKE) {
                noticeReactionRepository.delete(reaction)
                notice.decreaseDislike()
                noticeRepository.save(notice)
                return false
            } else {
                throw BusinessException(ErrorCode.ALREADY_LIKED)
            }
        } else {
            val newReaction = NoticeReactions(
                notice = notice,
                userId = userId,
                reactionType = ReactionType.DISLIKE
            )
            noticeReactionRepository.save(newReaction)
            notice.increaseDislike()
            noticeRepository.save(notice)
            return true
        }
    }

    @Transactional
    fun createComment(userId: Long, noticeId: Long, dto: CommentRequest) {
        val notice = getNotice(noticeId)
        val teamId = userService.getCurrentTeamId(userId)
        validateTeam(notice, teamId)

        commentService.createComment(
            userId = userId,
            teamId = teamId,
            postType = PostType.NOTICE,
            postId = noticeId,
            dto = dto,
            postTitle = notice.title,
            postWriterId = notice.userId
        )

        notice.increaseComment()
        noticeRepository.save(notice)
    }

    @Transactional
    fun deleteComment(userId: Long, noticeId: Long, commentId: Long) {
        val teamId = userService.getCurrentTeamId(userId)
        val notice = getNotice(noticeId)
        validateTeam(notice, teamId)

        val deletedCount = commentService.deleteComment(userId, PostType.NOTICE, noticeId, commentId, notice.userId)

        repeat(deletedCount) {
            notice.decreaseComment()
        }
        noticeRepository.save(notice)
    }

    private fun validateTeam(notice: Notices, teamId: Long) {
        if (notice.teamId != teamId) {
            throw BusinessException(ErrorCode.NOT_TEAM_MEMBER)
        }
    }

    private fun validateTitleAndContent(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            throw BusinessException(ErrorCode.TITLE_CONTENT_REQUIRED)
        }
    }

    private fun getNotice(noticeId: Long): Notices{
        return noticeRepository.findById(noticeId).orElseThrow {BusinessException(ErrorCode.POST_NOT_FOUND)}
    }

    private fun validateWriter(notice: Notices, userId: Long){
        if (notice.userId != userId) {
            throw BusinessException(ErrorCode.NOT_AUTHOR)
        }
    }
}
