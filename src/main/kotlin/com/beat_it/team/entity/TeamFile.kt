package com.beat_it.team.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.team.entity.enum.MediaCategory
import jakarta.persistence.*

@Entity
@Table(name = "team_files")
class TeamFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_file_id")
    val teamFileId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "original_file_name", nullable = false)
    val originalFileName: String,

    @Column(name = "storage_key", nullable = false)
    val storageKey: String,

    @Column(name = "cdn_url", nullable = false, length = 500)
    val cdnUrl: String,

    @Column(name = "preview_cdn_url", length = 500)
    val previewCdnUrl: String? = null,

    @Column(name = "mime_type", length = 100)
    val mimeType: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "media_category", nullable = false)
    val mediaCategory: MediaCategory,

    @Column(name = "file_size_bytes")
    val fileSizeBytes: Long? = null,

    @Column(name = "checksum", length = 100)
    val checksum: String? = null,

    @Column(name = "is_public", nullable = false)
    val isPublic: Boolean = false,
) : BaseCreatedTimeEntity() {
}