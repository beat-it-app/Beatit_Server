package com.beat_it.post.entity.meetit

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "meetit_response",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_meetit_participant",
            columnNames = ["meetit_participant_id", "slot_start_time"]
        )
    ])
class MeetitResponse(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meetit_response_id")
    val meetitResponseId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetit_id", nullable = false)
    val meetit: Meetit,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetit_participant_id", nullable = false)
    val meetitParticipant: MeetitParticipant,

    @Column(name = "slot_start_time", nullable = false)
    var slotStartTime: OffsetDateTime
)
