package com.beat_it.cal.entity

import jakarta.persistence.*

@Entity
@Table(name = "schedule_file")
class ScheduleFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 파일 엔티티 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    var schedule: Schedule? = null, // 어떤 일정의 파일인지

    @Column(nullable = false)
    val originalFileName: String, // 원본 파일명

    @Column(nullable = false)
    val storageKey: String, // 스토리지 저장 키

    @Column(nullable = false)
    val cdnUrl: String // CDN 접근 URL
)