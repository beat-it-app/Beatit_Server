package com.beat_it.post.entity.meetit

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "meetit_participant")
class MeetitParticipant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meetit_participant_id")
    val meetitParticipantId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetit_id", nullable = false)
    val meetit: Meetit,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "name", nullable = false)
    var name: String

) : BaseCreatedTimeEntity() {

    @OneToMany(mappedBy = "meetitParticipant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val responses: MutableList<MeetitResponse> = mutableListOf()

    fun addResponse(slotStartTime: java.time.OffsetDateTime) {
        val response = MeetitResponse(
            meetit = this.meetit,
            meetitParticipant = this,
            slotStartTime = slotStartTime
        )
        this.responses.add(response)
    }
}
