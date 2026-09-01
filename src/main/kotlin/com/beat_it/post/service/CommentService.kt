package com.beat_it.post.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.post.dto.CommentRequest
import com.beat_it.post.dto.CommentResponse
import com.beat_it.post.dto.MentionUserResponse
import com.beat_it.post.entity.PostCommentMentions
import com.beat_it.post.entity.PostComments
import com.beat_it.post.entity.enum.PostType
import com.beat_it.post.repository.PostCommentMentionRepository
import com.beat_it.post.repository.PostCommentRepository
import com.beat_it.team.repository.TeamMembershipRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val postCommentRepository: PostCommentRepository,
    private val postCommentMentionRepository: PostCommentMentionRepository,
    private val userService: UserService,
    private val teamMembershipRepository: TeamMembershipRepository,
) {

    private val mentionRegex = Regex("""@\{([^}]+)\}|@([a-zA-Z0-9가-힣_]+)""")

    @Transactional
    fun createComment(
        userId: Long,
        teamId: Long,
        postType: PostType,
        postId: Long,
        dto: CommentRequest
    ): PostComments {
        validateComment(dto.content)

        val comment = PostComments.createComment(
            postType = postType,
            postId = postId,
            userId = userId,
            content = dto.content
        )
        val savedComment = postCommentRepository.save(comment)
        saveMentions(savedComment, teamId, dto.content, dto.mentionedUserIds)

        return savedComment
    }

    @Transactional(readOnly = true)
    fun getComments(
        postType: PostType,
        postId: Long,
        postWriterId: Long,
        currentUserId: Long
    ): List<CommentResponse> {
        val comments = postCommentRepository.findByPostTypeAndPostIdOrderByCreatedAtAsc(postType, postId)
        if (comments.isEmpty()) return emptyList()

        val commentUserIds = comments.map { it.userId }

        val mentions = postCommentMentionRepository.findByCommentIn(comments)
        val mentionsByCommentId = mentions.groupBy { it.comment.commentId }
        val mentionedUserIds = mentions.map { it.mentionedUserId }

        val allUserIds = (commentUserIds + mentionedUserIds).distinct()
        val userProfilesMap = userService.getUserProfiles(allUserIds)
            .associateBy { it.userId }

        return comments.map { comment ->
            val writerProfile = userProfilesMap[comment.userId]
            val commentMentions = mentionsByCommentId[comment.commentId] ?: emptyList()
            val mentionedUserResponses = commentMentions.map { mention ->
                val mentionedProfile = userProfilesMap[mention.mentionedUserId]
                MentionUserResponse(
                    userId = mention.mentionedUserId,
                    name = mentionedProfile?.name ?: mention.mentionedName,
                    profileImageUrl = mentionedProfile?.profileImageUrl
                )
            }

            CommentResponse(
                commentId = comment.commentId!!,
                writerName = writerProfile?.name ?: "알 수 없음",
                content = comment.content,
                createdAt = DateTimeUtil.format(comment.createdAt),
                profileImageUrl = writerProfile?.profileImageUrl,
                isWriter = comment.userId == postWriterId,
                isMine = comment.userId == currentUserId,
                mentionedUsers = mentionedUserResponses
            )
        }
    }

    @Transactional
    fun deleteComment(
        userId: Long,
        postType: PostType,
        postId: Long,
        commentId: Long,
        postWriterId: Long
    ) {
        val comment = postCommentRepository.findById(commentId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }

        validateCommentBelongsToPost(comment, postType, postId)
        validateCommentDeletePermission(comment, userId, postWriterId)

        postCommentMentionRepository.deleteByComment(comment)
        postCommentRepository.delete(comment)
    }

    @Transactional
    fun deleteCommentsByPost(postType: PostType, postId: Long) {
        val comments = postCommentRepository.findByPostTypeAndPostIdOrderByCreatedAtAsc(postType, postId)
        if (comments.isNotEmpty()) {
            val commentIds = comments.mapNotNull { it.commentId }
            if (commentIds.isNotEmpty()) {
                postCommentMentionRepository.deleteByCommentCommentIdIn(commentIds)
            }
            postCommentRepository.deleteByPostTypeAndPostId(postType, postId)
        }
    }

    private fun saveMentions(
        comment: PostComments,
        teamId: Long,
        content: String,
        explicitMentionedUserIds: List<Long>?
    ) {
        val activeMembers = teamMembershipRepository.findAllByTeamTeamIdAndLeftAtIsNull(teamId)
        if (activeMembers.isEmpty()) return

        val activeMemberUserIds = activeMembers.map { it.userId }.toSet()
        val memberProfiles = userService.getUserProfiles(activeMemberUserIds.toList())
        val profileByName = memberProfiles.associateBy { it.name }
        val profileById = memberProfiles.associateBy { it.userId }

        val mentionsToSave = mutableListOf<PostCommentMentions>()
        val alreadyMentionedUserIds = mutableSetOf<Long>()

        // 1. 드롭다운에서 선택된 mentionedUserIds 처리
        explicitMentionedUserIds?.distinct()?.forEach { userId ->
            if (userId in activeMemberUserIds) {
                profileById[userId]?.let { profile ->
                    mentionsToSave.add(
                        PostCommentMentions.create(
                            comment = comment,
                            mentionedUserId = profile.userId,
                            mentionedName = profile.name
                        )
                    )
                    alreadyMentionedUserIds.add(profile.userId)
                }
            }
        }

        // 2. 본문 텍스트 내 @{username} 및 @username 파싱
        val extractedNames = extractMentionNames(content)
        extractedNames.forEach { name ->
            profileByName[name]?.let { profile ->
                if (profile.userId !in alreadyMentionedUserIds) {
                    mentionsToSave.add(
                        PostCommentMentions.create(
                            comment = comment,
                            mentionedUserId = profile.userId,
                            mentionedName = profile.name
                        )
                    )
                    alreadyMentionedUserIds.add(profile.userId)
                }
            }
        }

        if (mentionsToSave.isNotEmpty()) {
            postCommentMentionRepository.saveAll(mentionsToSave)
        }
    }

    private fun extractMentionNames(content: String): List<String> {
        val matches = mentionRegex.findAll(content)
        return matches.mapNotNull { match ->
            (match.groups[1]?.value ?: match.groups[2]?.value)?.trim()?.takeIf { it.isNotBlank() }
        }.distinct().toList()
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