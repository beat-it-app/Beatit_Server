package com.beat_it.cal.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "schedules")
class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    val scheduleId: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Column(name = "team_id", nullable = false)
    val teamId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(name = "starts_at", nullable = false)
    var startsAt: OffsetDateTime,

    @Column(name = "ends_at", nullable = false)
    var endsAt: OffsetDateTime,

    @Column(name = "location_id")
    var locationId: Long? = null,

    @Column(length = 500)
    var content: String? = null,

    @Column(name = "music_title")
    var musicTitle: String? = null,

    @Column(name = "music_artist")
    var musicArtist: String? = null,

    @Column(name = "music_preview_url")
    var musicPreviewUrl: String? = null

) : BaseUpdatedTimeEntity() {

    @OneToMany(mappedBy = "schedule", cascade = [CascadeType.ALL], orphanRemoval = true)
    val participants: MutableList<ScheduleParticipant> = mutableListOf()

    // TODO: S3 연동 후 File 엔티티와 연관관계 매핑 (1:N)
    @OneToMany(mappedBy = "schedule", cascade = [CascadeType.ALL], orphanRemoval = true)
    val files: MutableList<ScheduleFile> = mutableListOf()


    fun addParticipant(participantUserId: Long) {
        val participant = ScheduleParticipant(
            schedule = this,
            userId = participantUserId
        )
        this.participants.add(participant)
    }

    fun update(
        title: String,
        content: String?,
        locationId: Long?,
        startsAt: OffsetDateTime,
        endsAt: OffsetDateTime,
        musicTitle: String?,
        musicArtist: String?,
        musicPreviewUrl: String?
    ) {
        this.title = title
        this.content = content
        this.locationId = locationId
        this.startsAt = startsAt
        this.endsAt = endsAt
        this.musicTitle = musicTitle
        this.musicArtist = musicArtist
        this.musicPreviewUrl = musicPreviewUrl
    }

    fun isParticipantsSame(targetIds: List<Long>): Boolean {
        val currentIds = this.participants.map { it.userId }.sorted()
        val newIds = targetIds.sorted()
        return currentIds == newIds
    }

    fun addFile(originalFileName: String, storageKey: String, cdnUrl: String) {
        val scheduleFile = ScheduleFile(
            schedule = this,
            originalFileName = originalFileName,
            storageKey = storageKey,
            cdnUrl = cdnUrl
        )
        this.files.add(scheduleFile)
    }
}