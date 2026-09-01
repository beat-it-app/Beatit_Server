package com.beat_it.post.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.post.dto.CommentRequest
import com.beat_it.post.dto.CommentResponse
import com.beat_it.post.entity.PostComments
import com.beat_it.post.entity.enum.PostType
import com.beat_it.post.repository.PostCommentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val postCommentRepository: PostCommentRepository,
    private val userService: UserService,
) {

    @Transactional
    fun createComment(userId: Long, postType: PostType, postId: Long, dto: CommentRequest): PostComments {
        validateComment(dto.content)

        val comment = PostComments.createComment(
            postType = postType,
            postId = postId,
            userId = userId,
            content = dto.content
        )
        return postCommentRepository.save(comment)
    }

    @Transactional(readOnly = true)
    fun getComments(postType: PostType, postId: Long, postWriterId: Long, currentUserId: Long): List<CommentResponse> {
        val comments = postCommentRepository.findByPostTypeAndPostIdOrderByCreatedAtAsc(postType, postId)
        if (comments.isEmpty()) return emptyList()

        val userIds = comments.map { it.userId }.distinct()
        val userProfilesMap = userService.getUserProfiles(userIds)
            .associateBy { it.userId }

        return comments.map { comment ->
            val profile = userProfilesMap[comment.userId]
            CommentResponse(
                commentId = comment.commentId!!,
                writerName = profile?.name ?: "알 수 없음",
                content = comment.content,
                createdAt = DateTimeUtil.format(comment.createdAt),
                profileImageUrl = profile?.profileImageUrl,
                isWriter = comment.userId == postWriterId,
                isMine = comment.userId == currentUserId
            )
        }
    }

    @Transactional
    fun deleteComment(userId: Long, postType: PostType, postId: Long, commentId: Long, postWriterId: Long) {
        val comment = postCommentRepository.findById(commentId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }

        validateCommentBelongsToPost(comment, postType, postId)
        validateCommentDeletePermission(comment, userId, postWriterId)

        postCommentRepository.delete(comment)
    }

    @Transactional
    fun deleteCommentsByPost(postType: PostType, postId: Long) {
        postCommentRepository.deleteByPostTypeAndPostId(postType, postId)
    }

    private fun validateComment(comment: String) {
        if (comment.isBlank()) {
            throw BusinessException(ErrorCode.INVALID_COMMENT_CONTENT)
        }
    }

    private fun validateCommentBelongsToPost(comment: PostComments, postType: PostType, postId: Long) {
        if (comment.postType != postType || comment.postId != postId) {
            throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    private fun validateCommentDeletePermission(comment: PostComments, userId: Long, postOwnerId: Long) {
        if (comment.userId != userId && postOwnerId != userId) {
            throw BusinessException(ErrorCode.NOT_AUTHOR)
        }
    }
}