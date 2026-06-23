package com.beat_it.post.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.post.dto.CommentRequest
import com.beat_it.post.dto.CommentResponse
import com.beat_it.post.dto.DateItemResponse
import com.beat_it.post.dto.LocationItemResponse
import com.beat_it.post.dto.MusicItemResponse
import com.beat_it.post.dto.PollDetailResponse
import com.beat_it.post.dto.PollItems
import com.beat_it.post.dto.PollListResponse
import com.beat_it.post.dto.PollRequest
import com.beat_it.post.dto.TextItemResponse
import com.beat_it.post.entity.Polls
import com.beat_it.post.entity.PostComments
import com.beat_it.post.entity.enum.PollType
import com.beat_it.post.entity.enum.PostType
import com.beat_it.post.repository.PollRepository
import com.beat_it.post.repository.PollVoteRepository
import com.beat_it.post.repository.PostCommentRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

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

        val polls = pollRepository.getPolls(teamId, pageRequest)

        if (polls.isEmpty()) {
            return PollListResponse(pollListResponse = emptyList())
        }

        // N+1 문제 해결: 현재 가져온 pollId 목록 추출
        val pollIds = polls.map { it.pollId }.filterNotNull()

        val votedPollIds = pollRepository.findVotedPollIdsByUserIdAndPollIds(userId, pollIds).toSet()

        val pollItems = polls.map { poll ->
            PollItems(
                pollId = poll.pollId!!,
                teamId = teamId,
                title = poll.title,
                closeAt = DateTimeUtil.format(poll.closesAt!!),
                pollCount = poll.pollCount,
                isVoted = votedPollIds.contains(poll.pollId)
            )
        }
        return PollListResponse(pollListResponse = pollItems)
    }

    @Transactional
    fun postPoll(userId: Long, request: PollRequest){

    }

    @Transactional(readOnly = true)
    fun getPoll(userId: Long, pollId: Long): PollDetailResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId) // Polls 엔티티 조회
        validateTeam(poll, teamId)

        // 1. 투표 작성자 정보 조회
        val writerProfile = userService.getUserProfile(poll.userId)
        val writerName = writerProfile?.name ?: "알 수 없음"

        // 2. [PollVotes 반영] 로그인한 유저가 투표한 옵션 ID 목록 가져오기 (Set으로 만들어 O(1) 검색 최적화)
        val myVotedOptionIds = pollVoteRepository.findVotedOptionIdsByUserIdAndPollId(userId, pollId).toSet()

        // 3. [PollVotes 반영] 전체 옵션별 투표 수 집계 (N+1 방지하기 위해 Map으로 일괄 변환)
        // 결과 예시: { 1L (옵션ID): 5 (투표수), 2L: 3 }
        val voteCountsMap = pollVoteRepository.countVotesByPollId(pollId)
            .associate { row -> row[0] as Long to (row[1] as Long).toInt() }

        // 4. 투표 옵션 리스트 DTO 변환 (Polls 엔티티 내부의 pollOptions 목록을 순회한다고 가정)
        val pollItemResponses = poll.pollOptions.map { option ->
            val optionId = option.pollOptionId!!
            val voteCount = voteCountsMap[optionId] ?: 0 // 집계된 개수가 없으면 0개
            val isVoted = myVotedOptionIds.contains(optionId) // 내가 선택했는지 여부

            // pollType에 따른 다형성 매핑 (엔티티의 실제 필드명에 맞춰 수정 필요)
            when (poll.pollType) {
                PollType.TEXT -> TextItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    content = option.content ?: ""
                )
                PollType.DATE -> DateItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    date = DateTimeUtil.format(option.date)
                )
                PollType.MUSIC -> MusicItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    music = option.music ?: ""
                )
                PollType.LOCATION -> LocationItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted,
                    location = option.location ?: ""
                )
            }
        }

        // 5. 댓글 목록 조회 및 작성자 정보 맵핑 (이전과 동일)
        val comments = postCommentRepository.findByPostTypeAndPostIdOrderByCreatedAtAsc(PostType.POLL, pollId)
        val commenterIds = comments.map { it.userId }.distinct()
        val commenterProfilesMap = userService.getUserProfiles(commenterIds).associateBy { it.userId }

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

        // 6. 최종 응답 조립
        return PollDetailResponse(
            pollId = poll.pollId,
            title = poll.title,
            content = poll.content,
            pollType = poll.pollType,
            allowMultipleChoice = poll.allowMultipleChoice ?: false,
            isAnonymous = poll.isAnonymous ?: false,
            closesAt = DateTimeUtil.format(poll.closesAt),
            remindBeforeClose = DateTimeUtil.format(poll.remindBeforeClose),
            writerName = writerName,
            writerProfileImageUrl = writerProfile?.authFile?.cdnUrl,
            createdAt = DateTimeUtil.format(poll.createdAt),
            updatedAt = DateTimeUtil.format(poll.updatedAt),
            pollItems = pollItemResponses,
            isWriter = poll.userId == userId,
            commentCount = commentDtos.size,
            commentList = commentDtos
        )
    }

    @Transactional
    fun votePoll(userId: Long, request: PollRequest){

    }

    @Transactional
    fun deletePoll(userId: Long, pollId: Long){
        val teamId = userService.getCurrentTeamId(userId)
    }

    @Transactional
    fun createComment(userId: Long, pollId: Long, request: CommentRequest) {
        val poll = getPoll(pollId)
        val temaId = userService.getCurrentTeamId(userId)
        validateComment(request.content)
        validateTeam(poll, temaId)
        // Fixme: 17:18분에 생성했는데 8:13으로 찍힘. 시간 조정이 좀 필요해보임.

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