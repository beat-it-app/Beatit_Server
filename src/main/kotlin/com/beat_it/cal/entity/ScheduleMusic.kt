package com.beat_it.cal.entity

import jakarta.persistence.*

@Entity
@Table(name = "schedule_musics")
class ScheduleMusic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_music_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    val schedule: Schedule,

    @Column(name = "music_title")
    var musicTitle: String? = null,

    @Column(name = "music_artist")
    var musicArtist: String? = null,

    @Column(name = "music_preview_url")
    var musicPreviewUrl: String? = null
)
