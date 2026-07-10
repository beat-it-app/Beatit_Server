package com.beat_it.post.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.post.dto.*
import com.beat_it.post.entity.PollOptions
import com.beat_it.post.entity.PollVotes
import com.beat_it.post.entity.Polls
import com.beat_it.post.entity.PostComments
import com.beat_it.post.entity.enum.PollType
import com.beat_it.post.entity.enum.PostType
import com.beat_it.post.repository.PollRepository
import com.beat_it.post.repository.PollVoteRepository
import com.beat_it.post.repository.PostCommentRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class PollService(
    private val userService: UserService,
    private val pollRepository: PollRepository,
    private val postCommentRepository: PostCommentRepository,
    private val pollVoteRepository: PollVoteRepository,
) {
    @Transactional(readOnly = true)
    fun getPollList(userId: Long, page: Int = 0, size: Int = 10): PollListResponse{
        val teamId = userService.getCurrentTeamId(userId)
        val pageRequest = PageRequest.of(page, size)

        val pollsPage = pollRepository.getPolls(teamId, pageRequest)
        val polls = pollsPage.content

        if (polls.isEmpty()) {
            return PollListResponse(
                pollListResponse = emptyList(),
                totalCount = pollsPage.totalElements.toInt(),
                hasNext = pollsPage.hasNext()
            )
        }

        val pollIds = polls.map { it.pollId }.filterNotNull()
        val votedPollIds = pollRepository.findVotedPollIdsByUserIdAndPollIds(userId, pollIds).toSet()

        val pollItems = polls.map { poll ->
            PollItems(
                pollId = poll.pollId!!,
                teamId = teamId,
                title = poll.title,
                // 💡 수정 1: poll.closesAt은 Nullable(?), DateTimeUtil.format 함수는 Non-Null을 기대하므로 엘비스 처리
                closeAt = poll.closesAt?.let { DateTimeUtil.format(it) } ?: "",
                pollCount = poll.pollCount,
                isVoted = votedPollIds.contains(poll.pollId)
            )
        }
        return PollListResponse(
            pollListResponse = pollItems,
            totalCount = pollsPage.totalElements.toInt(),
            hasNext = pollsPage.hasNext()
        )
    }

    @Transactional
    fun postPoll(userId: Long, request: PollRequest){
        val teamId = userService.getCurrentTeamId(userId)

        val poll = Polls.postPoll(
            userId = userId,
            teamId = teamId,
            title = request.title,
            content = request.content,
            pollType = request.pollType,
            allowMultipleChoice = request.allowMultipleChoice ?: false,
            isAnonymous = request.isAnonymous ?: false,
            closesAt = request.closesAt,
            remindBeforeClose = request.remindBeforeClose ?: false
        )

        val options = request.pollList.mapIndexed { index, item ->
            val text = when (item) {
                is TextItem -> item.content
                is DateItem -> item.date?.let { DateTimeUtil.format(it) } ?: ""
                is MusicItem -> item.music
                is LocationItem -> item.location
                else -> ""
            }
            PollOptions(
                poll = poll,
                optionText = text,
                displayOrder = index
            )
        }

        poll.pollOptions = options
        pollRepository.save(poll)
    }

    @Transactional(readOnly = true)
    fun getPoll(userId: Long, pollId: Long): PollDetailResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)

        val writerProfile = userService.getUserProfile(poll.userId)
        val writerName = writerProfile?.name ?: "알 수 없음"

        val myVotedOptionIds = pollVoteRepository.findVotedOptionIdsByUserIdAndPollId(userId, pollId).toSet()

        val voteCountsMap = pollVoteRepository.countVotesByPollId(pollId)
            .associate { row -> row[0] as Long to (row[1] as Long).toInt() }

        val pollItemResponses = poll.pollOptions.map { option ->
            val optionId = option.pollOptionId!!
            val voteCount = voteCountsMap[optionId] ?: 0
            val isVoted = myVotedOptionIds.contains(optionId)

            when (poll.pollType) {
                PollType.TEXT -> TextItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    content = option.optionText
                )
                PollType.DATE -> DateItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    date = option.optionText
                )
                PollType.MUSIC -> MusicItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    music = option.optionText
                )
                PollType.LOCATION -> LocationItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    location = option.optionText
                )
            }
        }

        val comments = postCommentRepository.findByPostTypeAndPostIdOrderByCreatedAtAsc(PostType.POLL, pollId)

        val commentDtos = comments.map { comment ->
            val commentWriterProfile = userService.getUserProfile(comment.userId)
            CommentResponse(
                commentId = comment.commentId!!,
                writerName = commentWriterProfile?.name ?: "알 수 없음",
                content = comment.content,
                createdAt = DateTimeUtil.format(comment.createdAt),
                profileImageUrl = commentWriterProfile?.authFile?.cdnUrl,
                isWriter = comment.userId == poll.userId,
                isMine = comment.userId == userId
            )
        }

        return PollDetailResponse(
            pollId = poll.pollId!!,
            title = poll.title,
            content = poll.content,
            pollType = poll.pollType,
            allowMultipleChoice = poll.allowMultipleChoice,
            isAnonymous = poll.isAnonymous,
            closesAt = poll.closesAt?.let { DateTimeUtil.format(it) },
            remindBeforeClose = if (poll.remindBeforeClose) "REMIND" else "NONE",
            writerName = writerName,
            writerProfileImageUrl = writerProfile?.authFile?.cdnUrl ?: "",
            createdAt = DateTimeUtil.format(poll.createdAt),
            updatedAt = DateTimeUtil.format(poll.updatedAt),
            pollItems = pollItemResponses,
            isWriter = poll.userId == userId,
            commentCount = commentDtos.size,
            commentList = commentDtos
        )
    }

    @Transactional
    fun votePoll(userId: Long, pollId: Long, request: VoteRequest){
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)

        if (poll.closesAt != null && OffsetDateTime.now().isAfter(poll.closesAt)) {
            throw BusinessException(ErrorCode.POLL_CLOSED)
        }

        if (!poll.allowMultipleChoice && request.optionIds.size > 1) {
            throw BusinessException(ErrorCode.POLL_MULTIPLE_CHOICE_NOT_ALLOWED)
        }

        val pollOptionMap = poll.pollOptions.associateBy { it.pollOptionId }
        val targetOptions = request.optionIds.map { optionId ->
            pollOptionMap[optionId] ?: throw BusinessException(ErrorCode.POLL_OPTION_NOT_FOUND)
        }

        pollVoteRepository.deleteByUserIdAndPollId(userId, pollId)

        val newVotes = targetOptions.map { option ->
            PollVotes(
                poll = poll,
                pollOption = option,
                userId = userId
            )
        }
        pollVoteRepository.saveAll(newVotes)

        pollVoteRepository.flush()

        val participantCount = pollVoteRepository.countUniqueParticipantsByPollId(pollId).toInt()
        poll.pollCount = participantCount

        val voteCountsMap = pollVoteRepository.countVotesByPollId(pollId)
            .associate { row -> row[0] as Long to (row[1] as Long).toInt() }

        poll.pollOptions.forEach { option ->
            option.optionCount = voteCountsMap[option.pollOptionId] ?: 0
        }

        pollRepository.save(poll)
    }

    @Transactional
    fun deletePoll(userId: Long, pollId: Long){
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)
        validateWriter(poll, userId)

        pollVoteRepository.deleteByPollId(pollId)
        postCommentRepository.deleteByPostTypeAndPostId(PostType.POLL, pollId)
        pollRepository.delete(poll)
    }

    @Transactional
    fun createComment(userId: Long, pollId: Long, request: CommentRequest) {
        val poll = getPoll(pollId)
        val temaId = userService.getCurrentTeamId(userId)
        validateComment(request.content)
        validateTeam(poll, temaId)

        val comment = PostComments.createNoticeComment(
            noticeId = pollId,
            userId = userId,
            content = request.content
        )
        postCommentRepository.save(comment)

        poll.increaseComment()
        pollRepository.save(poll)
    }

    @Transactional
    fun deleteComment(userId: Long, pollId: Long){
        val teamId = userService.getCurrentTeamId(userId)
    }

    private fun getPoll(pollId: Long): Polls{
        return pollRepository.findById(pollId).orElseThrow {BusinessException(ErrorCode.POST_NOT_FOUND)}
    }

    private fun validateTeam(poll: Polls, teamId: Long) {
        if (poll.teamId != teamId) {
            throw BusinessException(ErrorCode.NOT_TEAM_MEMBER)
        }
    }

    private fun validateWriter(poll: Polls, userId: Long){
        if (poll.userId != userId) {
            throw BusinessException(ErrorCode.NOT_AUTHOR)
        }
    }

    private fun validateComment(comment: String) {
        if (comment.isBlank()) {
            throw BusinessException(ErrorCode.INVALID_COMMENT_CONTENT)
        }
    }
}