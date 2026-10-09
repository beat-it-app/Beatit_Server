package com.beat_it.post.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.post.dto.CommentRequest
import com.beat_it.post.dto.poll.*
import com.beat_it.post.entity.poll.*
import com.beat_it.post.repository.poll.*
import com.beat_it.post.entity.enum.PollType
import com.beat_it.post.entity.enum.PostType
import com.beat_it.location.entity.Locations
import com.beat_it.location.service.LocationsService
import com.beat_it.team.repository.TeamMembershipRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class PollService(
    private val userService: UserService,
    private val pollRepository: PollRepository,
    private val commentService: CommentService,
    private val pollVoteRepository: PollVoteRepository,
    private val locationsService: LocationsService,
    private val objectMapper: ObjectMapper,
    private val teamMembershipRepository: TeamMembershipRepository,
) {
    @Transactional(readOnly = true)
    fun getPollList(
        userId: Long,
        keyword: String? = null,
        page: Int = 0,
        size: Int = 10
    ): PollListResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val searchKeyword = keyword?.trim() ?: ""
        val now = OffsetDateTime.now()
        val pageable = PageRequest.of(page, size)

        val pollsPage = pollRepository.searchPolls(teamId, searchKeyword, now, pageable)
        val polls = pollsPage.content

        if (polls.isEmpty()) {
            return PollListResponse(
                pollListInProgress = emptyList(),
                pollListClosed = emptyList(),
                totalCount = pollsPage.totalElements.toInt(),
                hasNext = pollsPage.hasNext()
            )
        }

        val pollIds = polls.mapNotNull { it.pollId }
        val votedPollIds = pollRepository.findVotedPollIdsByUserIdAndPollIds(userId, pollIds).toSet()

        val (closedPolls, inProgressPolls) = polls.partition { poll ->
            poll.closeAt != null && now.isAfter(poll.closeAt)
        }

        fun Polls.toPollItem() = PollItems(
            pollId = this.pollId!!,
            title = this.title,
            closeAt = this.closeAt,
            pollCount = this.pollCount,
            isVoted = votedPollIds.contains(this.pollId)
        )

        return PollListResponse(
            pollListInProgress = inProgressPolls.map { it.toPollItem() },
            pollListClosed = closedPolls.map { it.toPollItem() },
            totalCount = pollsPage.totalElements.toInt(),
            hasNext = pollsPage.hasNext()
        )
    }

    @Transactional
    fun postPoll(userId: Long, request: PollRequest){
        validateCreatePoll(request)
        val teamId = userService.getCurrentTeamId(userId)

        val poll = Polls.postPoll(
            userId = userId,
            teamId = teamId,
            title = request.title,
            content = request.content,
            pollType = request.pollType,
            allowMultipleChoice = request.allowMultipleChoice ?: false,
            isAnonymous = request.isAnonymous ?: false,
            closeAt = request.closeAt,
            remindBeforeClose = request.remindBeforeClose ?: false
        )

        val options = createPollOptions(poll, request)
        poll.pollOptions = options.toMutableList()
        pollRepository.save(poll)
    }

    @Transactional
    fun updatePoll(userId: Long, pollId: Long, request: PollRequest){
        validateCreatePoll(request)
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)
        validateWriter(poll, userId)

        if (poll.pollCount > 0 || pollVoteRepository.countUniqueParticipantsByPollId(pollId) > 0) {
            throw BusinessException(ErrorCode.POLL_ALREADY_VOTED)
        }

        val newOptions = createPollOptions(poll, request)
        poll.updatePoll(
            title = request.title,
            content = request.content,
            pollType = request.pollType,
            allowMultipleChoice = request.allowMultipleChoice ?: false,
            isAnonymous = request.isAnonymous ?: false,
            remindBeforeClose = request.remindBeforeClose ?: false,
            closeAt = request.closeAt,
            newOptions = newOptions
        )
        pollRepository.save(poll)
    }

    private fun createPollOptions(poll: Polls, request: PollRequest): List<PollOptions> {
        return request.pollList.mapIndexed { index, item ->
            var locationEntity: Locations? = null
            var metadata: String? = null
            val text = when (request.pollType) {
                PollType.TEXT -> item.content ?: ""
                PollType.MUSIC -> {
                    if (item.music != null) {
                        metadata = objectMapper.writeValueAsString(item.music)
                        "${item.music.title} - ${item.music.artist}"
                    } else {
                        ""
                    }
                }
                PollType.LOCATION -> {
                    if (item.locationId != null) {
                        val loc = locationsService.findLocation(item.locationId)
                        locationEntity = loc
                        loc.locationName ?: loc.roadAddress ?: item.location ?: ""
                    } else {
                        item.location ?: ""
                    }
                }
            }
            PollOptions(
                poll = poll,
                optionText = text,
                optionMetadata = metadata,
                displayOrder = index,
                location = locationEntity
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPoll(userId: Long, pollId: Long): PollDetailResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)

        val writerProfile = userService.getUserProfileResponse(poll.userId)
        val writerName = writerProfile?.name ?: "알 수 없음"

        val myVotedOptionIds = pollVoteRepository.findVotedOptionIdsByUserIdAndPollId(userId, pollId).toSet()

        val voteCountsMap = pollVoteRepository.countVotesByPollId(pollId)
            .associate { row -> row[0] as Long to (row[1] as Long).toInt() }

        val votersMap: Map<Long, List<PollVoterResponse>>? = if (!poll.isAnonymous) {
            val optionVotes = pollVoteRepository.findOptionVotesByPollId(pollId)
            val voterUserIds = optionVotes.map { it[1] as Long }.distinct()
            val userProfiles = userService.getUserProfiles(voterUserIds).associateBy { it.userId }
            val memberPositions = teamMembershipRepository
                .findAllByTeamTeamIdAndUserIdInAndLeftAtIsNull(poll.teamId, voterUserIds)
                .associate { it.userId to it.position }

            optionVotes.groupBy(
                keySelector = { it[0] as Long },
                valueTransform = { row ->
                    val voterId = row[1] as Long
                    val profile = userProfiles[voterId]
                    PollVoterResponse(
                        userId = voterId,
                        name = profile?.name ?: "알 수 없음",
                        profileImageUrl = profile?.profileImageUrl,
                        position = memberPositions[voterId]
                    )
                }
            )
        } else {
            null
        }

        val pollItemResponses = poll.pollOptions.map { option ->
            val optionId = option.pollOptionId!!
            val voteCount = voteCountsMap[optionId] ?: 0
            val isVoted = myVotedOptionIds.contains(optionId)
            val voters = votersMap?.get(optionId) ?: if (!poll.isAnonymous) emptyList() else null

            when (poll.pollType) {
                PollType.TEXT -> TextItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted, voters = voters,
                    content = option.optionText
                )
                PollType.MUSIC -> {
                    val musicInfo = option.optionMetadata?.let {
                        try {
                            objectMapper.readValue(it, PollMusicRequest::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    MusicItemResponse(
                        itemId = optionId,
                        voteCount = voteCount,
                        isVoted = isVoted,
                        voters = voters,
                        title = musicInfo?.title ?: option.optionText,
                        artist = musicInfo?.artist ?: "Unknown Artist",
                        previewUrl = musicInfo?.previewUrl
                    )
                }
                PollType.LOCATION -> LocationItemResponse(
                    itemId = optionId, voteCount = voteCount, isVoted = isVoted, voters = voters,
                    location = option.optionText,
                    locationId = option.location?.locationId,
                    locationName = option.location?.locationName,
                    roadAddress = option.location?.roadAddress,
                    latitude = option.location?.latitude,
                    longitude = option.location?.longitude,
                    mapUrl = option.location?.mapUrl,
                    phone = option.location?.phone,
                    kakaoPlaceId = option.location?.kakaoPlaceId,
                    jibunAddress = option.location?.jibunAddress
                )
            }
        }

        val comments = commentService.getComments(
            postType = PostType.POLL,
            postId = pollId,
            postWriterId = poll.userId,
            currentUserId = userId
        )

        return PollDetailResponse(
            pollId = poll.pollId!!,
            title = poll.title,
            content = poll.content,
            pollType = poll.pollType,
            allowMultipleChoice = poll.allowMultipleChoice,
            isAnonymous = poll.isAnonymous,
            closeAt = poll.closeAt,
            remindBeforeClose = if (poll.remindBeforeClose) "REMIND" else "NONE",
            writerName = writerName,
            writerProfileImageUrl = writerProfile?.profileImageUrl ?: "",
            createdAt = poll.createdAt,
            updatedAt = poll.updatedAt,
            pollItems = pollItemResponses,
            isWriter = poll.userId == userId,
            commentCount = comments.size,
            commentList = comments
        )
    }

    @Transactional
    fun votePoll(userId: Long, pollId: Long, request: VoteRequest){
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)

        if (poll.closeAt != null && OffsetDateTime.now().isAfter(poll.closeAt)) {
            throw BusinessException(ErrorCode.POLL_CLOSED)
        }

        if (request.optionIds.isEmpty()) {
            throw BusinessException(ErrorCode.POLL_OPTION_REQUIRED)
        }

        val distinctOptionIds = request.optionIds.distinct()

        if (!poll.allowMultipleChoice && distinctOptionIds.size > 1) {
            throw BusinessException(ErrorCode.POLL_MULTIPLE_CHOICE_NOT_ALLOWED)
        }

        val pollOptionMap = poll.pollOptions.associateBy { it.pollOptionId }
        val targetOptions = distinctOptionIds.map { optionId ->
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
    fun cancelVote(userId: Long, pollId: Long) {
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)

        if (poll.closeAt != null && OffsetDateTime.now().isAfter(poll.closeAt)) {
            throw BusinessException(ErrorCode.POLL_CLOSED)
        }

        val votedOptionIds = pollVoteRepository.findVotedOptionIdsByUserIdAndPollId(userId, pollId)
        if (votedOptionIds.isEmpty()) {
            throw BusinessException(ErrorCode.POLL_NOT_VOTED)
        }

        pollVoteRepository.deleteByUserIdAndPollId(userId, pollId)
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
        commentService.deleteCommentsByPost(PostType.POLL, pollId)
        pollRepository.delete(poll)
    }

    @Transactional
    fun createComment(userId: Long, pollId: Long, request: CommentRequest) {
        val poll = getPoll(pollId)
        val teamId = userService.getCurrentTeamId(userId)
        validateTeam(poll, teamId)

        commentService.createComment(userId, teamId, PostType.POLL, pollId, request)

        poll.increaseComment()
        pollRepository.save(poll)
    }

    @Transactional
    fun deleteComment(userId: Long, pollId: Long, commentId: Long){
        val teamId = userService.getCurrentTeamId(userId)
        val poll = getPoll(pollId)
        validateTeam(poll, teamId)

        val deletedCount = commentService.deleteComment(userId, PostType.POLL, pollId, commentId, poll.userId)

        repeat(deletedCount) {
            poll.decreaseComment()
        }
        pollRepository.save(poll)
    }

    private fun getPoll(pollId: Long): Polls{
        return pollRepository.findById(pollId).orElseThrow {BusinessException(ErrorCode.POST_NOT_FOUND)}
    }

    private fun validateTeam(poll: Polls, teamId: Long) {
        if (poll.teamId != teamId) {
            throw BusinessException(ErrorCode.NOT_TEAM_MEMBER)
        }
    }

    private fun validateCreatePoll(request: PollRequest) {
        if (request.title.isBlank()) {
            throw BusinessException(ErrorCode.TITLE_CONTENT_REQUIRED)
        }
        if (request.pollList.size < 2) {
            throw BusinessException(ErrorCode.INVALID_REQUEST_BODY)
        }
        request.pollList.forEach { item ->
            val isValid = when (request.pollType) {
                PollType.TEXT -> !item.content.isNullOrBlank()
                PollType.MUSIC -> item.music != null && item.music.title.isNotBlank()
                PollType.LOCATION -> !item.location.isNullOrBlank() || item.locationId != null
            }
            if (!isValid) {
                throw BusinessException(ErrorCode.INVALID_REQUEST_BODY)
            }
        }
        if (request.closeAt != null && OffsetDateTime.now().isAfter(request.closeAt)) {
            throw BusinessException(ErrorCode.CALENDAR_INVALID_TIME_RANGE)
        }
    }

    private fun validateWriter(poll: Polls, userId: Long){
        if (poll.userId != userId) {
            throw BusinessException(ErrorCode.NOT_AUTHOR)
        }
    }
}