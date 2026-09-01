package com.beat_it.post.repository.poll

import com.beat_it.post.entity.poll.Polls
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface PollRepository : JpaRepository<Polls, Long> {

    @Query("""
        SELECT p FROM Polls p 
        WHERE p.teamId = :teamId 
        AND (:keyword = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR (p.content IS NOT NULL AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        ORDER BY 
            CASE 
                WHEN p.closeAt IS NULL OR p.closeAt > :now THEN 0 
                ELSE 1 
            END ASC,
            p.createdAt DESC
    """)
    fun searchPolls(
        @Param("teamId") teamId: Long,
        @Param("keyword") keyword: String,
        @Param("now") now: OffsetDateTime,
        pageable: Pageable
    ): Page<Polls>

    @Query("SELECT v.poll.pollId FROM PollVotes v WHERE v.userId = :userId AND v.poll.pollId IN :pollIds")
    fun findVotedPollIdsByUserIdAndPollIds(
        @Param("userId") userId: Long,
        @Param("pollIds") pollIds: List<Long>
    ): List<Long>
}