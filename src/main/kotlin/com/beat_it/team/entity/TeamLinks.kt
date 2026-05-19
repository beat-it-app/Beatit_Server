package com.beat_it.team.entity

import com.beat_it.team.entity.enum.PlatformCode
import com.beat_it.team.entity.enum.TeamRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_links")
class TeamLinks(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="team_link_id", nullable = false)
    val teamLinkId: Long? = null,

    //TODO: 팀 ID 연결하기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id", nullable = false)
    val teams: Teams = Teams(),

    @Column(name="part_name",nullable = false)
    var partName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name="platform_code",nullable = false)
    var platformCode: PlatformCode = PlatformCode.CUSTOM,

    @Column(name="link_url",nullable = false)
    var linkUrl: String = "",

    @Column(name="update_at",nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name="create_at",nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    )
//    ) : BaseTime