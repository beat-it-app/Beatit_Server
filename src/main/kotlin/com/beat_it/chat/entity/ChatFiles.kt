package com.beat_it.chat.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*
enum class MediaCategory {
    IMAGE, AUDIO, VIDEO, DOCUMENT
}

@Entity
@Table(name = "chat_files")
class ChatFiles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_files_id")
    val chatFilesId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "original_file_name", nullable = false, length = 255)
    val originalFileName: String,

    @Column(name = "storage_key", nullable = false, length = 255)
    val storageKey: String,

    @Column(name = "cdn_url", nullable = false, length = 500)
    val cdnUrl: String,

    @Column(name = "preview_cdn_url", length = 500)
    var previewCdnUrl: String? = null,

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
    var isPublic: Boolean = false,

) : BaseCreatedTimeEntity()