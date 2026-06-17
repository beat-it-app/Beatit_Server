package com.beat_it.post.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.post.dto.*
import com.beat_it.post.entity.NoticeAttachments
import com.beat_it.post.entity.Notices
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
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.global.service.FileService

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val fileService: FileService,
    private val noticeAttachmentsRepository: NoticeAttachmentsRepository,
    private val noticeReactionRepository: NoticeReactionRepository,
    private val postCommentRepository: PostCommentRepository,
    private val postFilesRepository: PostFilesRepository,
    private val userService: UserService
) {

    @Transactional(readOnly = true)
    fun getNoticeList(userId: Long, keyword: String?, sortStr: String): NoticeListResponse? {
        val teamId = userService.getCurrentTeamId(userId)

        val sort = if (sortStr.uppercase() == "OLDEST") {
            Sort.by(Sort.Direction.ASC, "createdAt")
        } else {
            Sort.by(Sort.Direction.DESC, "createdAt")
        }

        val searchKeyword = keyword ?: ""
        // FIXME : 한 번에 몇 개씩 불러올지 정해야 함 (paging 처리 같은 거 해야해)
        val notices = noticeRepository.searchNotices(teamId, searchKeyword, sort)

        if (notices.isEmpty()) {
            return null
        }

        val noticeItems = notices.map { notice ->
            val userProfile = userService.getUserProfile(notice.userId)
            val writerName = userProfile?.name ?: "알 수 없음"

            val description = if (notice.content.length > 20) {
                "${notice.content.substring(0, 20)}..."
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
                createdAt = DateTimeUtil.format(notice.createdAt),
                writer = writerName,
                thumbnailUrl = notice.thumbnailImageUrl
            )
        }

        return NoticeListResponse(noticeListResponse = noticeItems)
    }

    @Transactional
    fun createNotice(userId: Long, dto: NoticeRequest, images: List<MultipartFile>?) {
        validateTitleAndContent(dto.title, dto.content)
        val teamId = userService.getCurrentTeamId(userId)

        val uploadedPostFiles = uploadAndSavePostFiles(userId, images)
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
    }

    @Transactional
    fun getNotice(userId: Long, noticeId: Long): NoticeDetailResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val notice = getNotice(noticeId)
        validateTeam(notice, teamId)

        val writerProfile = userService.getUserProfile(notice.userId)
        val writerName = writerProfile?.name ?: "알 수 없음"

        val attachments = noticeAttachmentsRepository.findByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId)
        val imageUrls = attachments.map { it.postFile.cdnUrl }

        val userReaction = noticeReactionRepository.findByNoticeNoticeIdAndUserId(noticeId, userId)
        val isLiked = userReaction.map { it.reactionType == ReactionType.LIKE }.orElse(false)
        val isDisliked = userReaction.map { it.reactionType == ReactionType.DISLIKE }.orElse(false)

        val comments = postCommentRepository.findByPostTypeAndPostIdOrderByCreatedAtAsc(PostType.NOTICE, noticeId)

        val reactionDto = NoticeReactionDto(
            likeCount = notice.likeCounter,
            dislikeCount = notice.dislikeCounter,
            isLiked = isLiked,
            isDisliked = isDisliked,
            commentCount = comments.size
        )

        // FIXME : n+1 문제가 발생할 수 있음. 검증해볼 것.
        val commentDtos = comments.map { comment ->
            val commentWriterProfile = userService.getUserProfile(comment.userId)
            NoticeCommentDto(
                commentId = comment.commentId!!,
                writerName = commentWriterProfile?.name ?: "알 수 없음",
                content = comment.content,
                createdAt = DateTimeUtil.format(comment.createdAt),
                profileImageUrl = commentWriterProfile?.authFile?.cdnUrl,
                isWriter = comment.userId == notice.userId,
                isMine = comment.userId == userId
            )
        }

        return NoticeDetailResponse(
            noticeId = notice.noticeId!!,
            title = notice.title,
            content = notice.content,
            writerName = writerName,
            writerProfileImageUrl = writerProfile?.authFile?.cdnUrl,
            createdAt = DateTimeUtil.format(notice.createdAt),
            updatedAt = DateTimeUtil.format(notice.updatedAt),
            images = imageUrls,
            isWriter = notice.userId == userId,
            reaction = reactionDto,
            commentList = commentDtos
        )
    }

    @Transactional
    fun editNotice(userId: Long, noticeId: Long, dto: NoticeRequest, images: List<MultipartFile>?) {
        validateTitleAndContent(dto.title, dto.content)
        val teamId = userService.getCurrentTeamId(userId)

        val notice = getNotice(noticeId)
        validateTeam(notice, teamId)
        validateWriter(notice, userId)

        val existingAttachments = noticeAttachmentsRepository.findByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId)
        val isImagesSame = images == null || (images.isEmpty() && existingAttachments.isEmpty())

        if (notice.title == dto.title && notice.content == dto.content && isImagesSame) {
            throw BusinessException(ErrorCode.POST_NO_CONTENT_TO_UPDATE)
        }

        var thumbnailUrl = notice.thumbnailImageUrl

        images?.let { multipartFiles ->
            deleteNoticeAttachments(existingAttachments, noticeId)

            val uploadedPostFiles = uploadAndSavePostFiles(userId, multipartFiles)
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

        val existingAttachments = noticeAttachmentsRepository.findByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId)
        deleteNoticeAttachments(existingAttachments, noticeId)

        noticeReactionRepository.deleteByNoticeNoticeId(noticeId)
        postCommentRepository.deleteByPostTypeAndPostId(PostType.NOTICE, noticeId)
        noticeRepository.delete(notice)
    }

    private fun uploadAndSavePostFiles(userId: Long, images: List<MultipartFile>?): List<PostFiles> {
        val uploadedFiles = images?.let { fileService.uploadFiles(it, "notice") } ?: emptyList()
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

    private fun deleteNoticeAttachments(attachments: List<NoticeAttachments>, noticeId: Long) {
        attachments.forEach { attachment ->
            attachment.postFile.delete()
            postFilesRepository.save(attachment.postFile)
        }
        noticeAttachmentsRepository.deleteByNoticeNoticeId(noticeId)
    }

    private fun validateTitleAndContent(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            throw BusinessException(ErrorCode.TITLE_CONTENT_REQUIRED)
        }
    }

    private fun getNotice(noticeId: Long): Notices{
        return noticeRepository.findById(noticeId).orElseThrow {BusinessException(ErrorCode.POST_NOT_FOUND)}
    }

    private fun validateTeam(notice: Notices, teamId: Long) {
        if (notice.teamId != teamId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateWriter(notice: Notices, userId: Long){
        if (notice.userId != userId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }
}