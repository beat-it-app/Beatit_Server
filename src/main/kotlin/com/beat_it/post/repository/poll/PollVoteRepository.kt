package com.beat_it.post.repository.poll

import com.beat_it.post.entity.poll.PollVotes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PollVoteRepository : JpaRepository<PollVotes, Long> {

    @Query("SELECT pv.pollOption.pollOptionId FROM PollVotes pv WHERE pv.userId = :userId AND pv.poll.pollId = :pollId")
    fun findVotedOptionIdsByUserIdAndPollId(
        @Param("userId") userId: Long,
        @Param("pollId") pollId: Long
    ): List<Long>

    @Query("SELECT pv.pollOption.pollOptionId, COUNT(pv) FROM PollVotes pv WHERE pv.poll.pollId = :pollId GROUP BY pv.pollOption.pollOptionId")
    fun countVotesByPollId(@Param("pollId") pollId: Long): List<Array<Any>>

    @Modifying
    @Query("DELETE FROM PollVotes pv WHERE pv.userId = :userId AND pv.poll.pollId = :pollId")
    fun deleteByUserIdAndPollId(@Param("userId") userId: Long, @Param("pollId") pollId: Long)

    @Modifying
    @Query("DELETE FROM PollVotes pv WHERE pv.poll.pollId = :pollId")
    fun deleteByPollId(@Param("pollId") pollId: Long)

    @Query("SELECT COUNT(DISTINCT pv.userId) FROM PollVotes pv WHERE pv.poll.pollId = :pollId")
    fun countUniqueParticipantsByPollId(@Param("pollId") pollId: Long): Long
}