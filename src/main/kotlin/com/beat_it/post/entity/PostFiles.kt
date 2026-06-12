package com.beat_it.post.entity

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.global.entity.BaseDeletedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "post_files")
class PostFiles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_file_id")
    val postFileId: Long? = null,

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
    var isPublic: Boolean = false

) : BaseDeletedTimeEntity()
