package com.beat_it.team.entity

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

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

    @Column(name = "original_file_name", nullable = false, length = 255)
    var originalFileName: String,

    @Column(name = "storage_key", nullable = false, length = 255)
    var storageKey: String,

    @Column(name = "cdn_url", nullable = false, length = 500)
    var cdnUrl: String,

    @Column(name = "preview_cdn_url", length = 500)
    var previewCdnUrl: String? = null,

    @Column(name = "mime_type", length = 100)
    var mimeType: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "media_category", nullable = false)
    var mediaCategory: MediaCategory,

    @Column(name = "file_size_bytes")
    var fileSizeBytes: Long? = null,

    @Column(length = 100)
    var checksum: String? = null,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,
) : BaseCreatedTimeEntity()