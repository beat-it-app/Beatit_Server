package com.beat_it.performance.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "invitation_card")
class InvitationCard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_card_id", nullable = false)
    var invitationCardId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false, unique = true)
    var performance: Performances,

    @Column(name = "performance_team", length = 100, nullable = false)
    var performanceTeam: String,

    @Column(name = "description", length = 150, nullable = true)
    var description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "setlist", columnDefinition = "json")
    var setlist: String? = null

) : BaseUpdatedTimeEntity() {

    fun update(
        performanceTeam: String?,
        description: String?,
        setlist: String?
    ) {
        performanceTeam?.let { this.performanceTeam = it }
        this.description = description
        this.setlist = setlist
        this.updateTimestamp()
    }
}