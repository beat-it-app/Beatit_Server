package com.beat_it.post.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "poll_votes",
    // 성능 향상 및 정합성을 위해 유저가 동일한 선택지에 중복 데이터(Row)를 넣지 못하도록 유니크 제약조건 설정
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
    var poll: Poll,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_option_id", nullable = false)
    var pollOption: PollOptions,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

): BaseCreatedTimeEntity() {
}