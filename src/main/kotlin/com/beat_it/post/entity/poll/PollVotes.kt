package com.beat_it.post.entity.poll

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "poll_votes",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_poll_option",
            columnNames = ["user_id", "poll_option_id"]
        )
    ])
class PollVotes (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_vote_id")
    val pollVoteId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    var poll: Polls,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_option_id", nullable = false)
    var pollOption: PollOptions,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

): BaseCreatedTimeEntity() {
}