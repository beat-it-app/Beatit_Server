package com.beat_it.performance.entity

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.performance.entity.enum.PerformanceFileType
import jakarta.persistence.*

@Entity
@Table(name = "performance_files")
class PerformanceFiles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_file_id", nullable = false)
    val performanceFileId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    val performance: Performances,

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

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    var fileType: PerformanceFileType,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "file_size_bytes")
    var fileSizeBytes: Long? = null,

    @Column(length = 100)
    var checksum: String? = null,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,

) : BaseCreatedTimeEntity()
