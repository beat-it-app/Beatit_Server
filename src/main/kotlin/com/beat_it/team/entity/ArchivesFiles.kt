package com.beat_it.team.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "archives_files")
class ArchivesFiles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_file_id", nullable = false)
    val archiveFileId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    val archive: Archives,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    //TODO: 팀 저장 파일 만들기

): BaseCreatedTimeEntity() {}
