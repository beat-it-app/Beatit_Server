package com.beat_it.post.entity.poll

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

import com.beat_it.location.entity.Locations

@Entity
@Table(name = "poll_options")
class PollOptions (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_option_id")
    val pollOptionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    var poll: Polls,

    @Column(name = "option_text", nullable = false)
    var optionText: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "option_metadata", columnDefinition = "json")
    var optionMetadata: String? = null,

    @Column(name = "option_count", nullable = false)
    var optionCount: Int = 0,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = true)
    var location: Locations? = null,
){
}