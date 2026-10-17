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

    @Column(name = "booking_link", length = 500)
    var bookingLink: String? = null,

    @Column(name = "host_name", length = 20)
    var hostName: String,

    @Column(name = "host_contact", length = 255)
    var hostContact: String? = null,

    @Column(name = "host_link", length = 500)
    var hostLink: String? = null,

    @Column(name = "detail_info", columnDefinition = "TEXT", length = 1000)
    var detailInfo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_status", nullable = false)
    var performanceStatus: PerformanceStatus = PerformanceStatus.UPCOMING,

) : BaseUpdatedTimeEntity() {

    @OneToMany(mappedBy = "performance", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    val files: MutableList<PerformanceFiles> = mutableListOf()

    @OneToMany(mappedBy = "performance", cascade = [CascadeType.ALL], orphanRemoval = true)
    val prices: MutableList<PerformancePrices> = mutableListOf()

    @OneToOne(mappedBy = "performance", cascade = [CascadeType.ALL], orphanRemoval = true)
    var invitationCard: InvitationCard? = null

    fun updatePerformance(
        title: String?,
        performanceDateTime: OffsetDateTime?,
        location: Locations?,
        placeName: String?,
        posterFileId: Long?,
        description: String?,
        detailInfo: String?,
        bookingDeadline: OffsetDateTime?,
        bookingLink: String?,
        hostName: String?,
        hostContact: String?,
        hostLink: String?
    ) {
        title?.let { this.title = it }
        performanceDateTime?.let {
            this.performanceDateTime = it
            this.performanceStatus = if (it.isAfter(OffsetDateTime.now())) PerformanceStatus.UPCOMING else PerformanceStatus.PAST
        }
        location?.let { this.location = it }
        placeName?.let { this.placeName = it }
        posterFileId?.let { this.posterFileId = it }
        description?.let { this.description = it }
        detailInfo?.let { this.detailInfo = it }
        bookingDeadline?.let { this.bookingDeadline = it }
        bookingLink?.let { this.bookingLink = it }
        hostName?.let { this.hostName = it }
        this.hostContact = hostContact
        this.hostLink = hostLink
        this.updateTimestamp()
    }

    fun updateStatus(status: PerformanceStatus) {
        this.performanceStatus = status
    }
}
