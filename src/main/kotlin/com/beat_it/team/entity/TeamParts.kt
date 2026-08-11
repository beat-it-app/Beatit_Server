package com.beat_it.team.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "team_parts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_team_parts_team_user",
            columnNames = ["team_id", "user_id"]
        )
    ]
)
class TeamParts(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="team_part_id", nullable = false)
    val teamPartId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name="part_name", nullable = false)
    var partName: String = "",

    @Column(name="display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name="is_active", nullable = false)
    var isActive: Boolean = true,

    ) : BaseUpdatedTimeEntity() {
    fun updateTeamPart(
        partName: String,
        displayOrder: Int,
    ) {
        this.partName = partName
        this.displayOrder = displayOrder
    }

    fun deactivateTeamPart() {
        this.isActive = false
    }

    fun activateTeamPart() {
        this.isActive = true
    }
}
