package com.beat_it.post.entity.meetit

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalTime

@Entity
@Table(name = "meetit")
class Meetit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meetit_id")
    val meetitId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "team_id", nullable = false)
    val teamId: Long,

    @Column(name = "title", nullable = false)
    var title: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "candidate_dates", columnDefinition = "json", nullable = false)
    var candidateDates: String,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime,

    @Column(name = "total_invited_count", nullable = false)
    var totalInvitedCount: Int

) : BaseCreatedTimeEntity() {

    @OneToMany(mappedBy = "meetit", cascade = [CascadeType.ALL], orphanRemoval = true)
    val participants: MutableList<MeetitParticipant> = mutableListOf()

    fun addParticipant(userId: Long, name: String) {
        val participant = MeetitParticipant(
            meetit = this,
            userId = userId,
            name = name
        )
        this.participants.add(participant)
    }
}
