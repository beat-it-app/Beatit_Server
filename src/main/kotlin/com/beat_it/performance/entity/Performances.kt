package com.beat_it.performance.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.location.entity.Locations
import com.beat_it.performance.entity.enum.PerformanceStatus
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "performances")
class Performances(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id", nullable = false)
    val performanceId: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Column(name = "team_id", nullable = false)
    val teamId: Long,

    @Column(name = "created_user_id", nullable = false)
    val createdUserId: Long,

    @Column(name = "title", length = 200, nullable = false)
    var title: String,

    @Column(name = "performance_date_time", nullable = false)
    var performanceDateTime: OffsetDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = true)
    var location: Locations? = null,

    @Column(name = "place_name", length = 200, nullable = true)
    var placeName: String? = null,

    @Column(name = "poster_file_id", nullable = true)
    var posterFileId: Long? = null,

    @Column(name = "description", columnDefinition = "TEXT", length = 200, nullable = true)
    var description: String? = null,

    @Column(name = "booking_deadline", nullable = true)
    var bookingDeadline: OffsetDateTime? = null,

    @Column(name = "booking_link", length = 500, nullable = true)
    var bookingLink: String? = null,

    @Column(name = "host_name", length = 100, nullable = false)
    var hostName: String,

    @Column(name = "host_contact", length = 255, nullable = true)
    var hostContact: String? = null,

    @Column(name = "host_link", length = 500, nullable = true)
    var hostLink: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_status", nullable = false)
    var performanceStatus: PerformanceStatus = PerformanceStatus.PUBLISHED,

) : BaseUpdatedTimeEntity() {

    @OneToMany(mappedBy = "performance", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    val files: MutableList<PerformanceFiles> = mutableListOf()

    @OneToMany(mappedBy = "performance", cascade = [CascadeType.ALL], orphanRemoval = true)
    val prices: MutableList<PerformancePrices> = mutableListOf()

    fun updatePerformance(
        title: String?,
        performanceDateTime: OffsetDateTime?,
        location: Locations?,
        placeName: String?,
        posterFileId: Long?,
        description: String?,
        bookingDeadline: OffsetDateTime?,
        bookingLink: String?,
        hostName: String?,
        hostContact: String?,
        hostLink: String?,
        performanceStatus: PerformanceStatus?
    ) {
        title?.let { this.title = it }
        performanceDateTime?.let { this.performanceDateTime = it }
        location?.let { this.location = it }
        placeName?.let { this.placeName = it }
        posterFileId?.let { this.posterFileId = it }
        description?.let { this.description = it }
        bookingDeadline?.let { this.bookingDeadline = it }
        bookingLink?.let { this.bookingLink = it }
        hostName?.let { this.hostName = it }
        hostContact?.let { this.hostContact = it }
        hostLink?.let { this.hostLink = it }
        performanceStatus?.let { this.performanceStatus = it }
    }

    fun updateStatus(status: PerformanceStatus) {
        this.performanceStatus = status
    }
}
