package com.beat_it.cal.entity

import jakarta.persistence.*

@Entity
@Table(name = "schedule_file")
class ScheduleFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    var schedule: Schedule? = null,

    @Column(nullable = false)
    val originalFileName: String,

    @Column(nullable = false)
    val storageKey: String,

    @Column(nullable = false)
    val cdnUrl: String
)