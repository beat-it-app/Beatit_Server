package com.beat_it.auth.entity

import jakarta.persistence.*
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.global.entity.BaseCreatedTimeEntity
import java.time.OffsetDateTime

@Entity
@Table(name = "auth_files")
class AuthFiles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authfiles_id")
    val authFileId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: Users, // FK 설정

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