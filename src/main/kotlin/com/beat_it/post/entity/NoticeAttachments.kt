package com.beat_it.post.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.post.entity.enum.FileType
import jakarta.persistence.*

@Entity
@Table(name = "notice_attachments")
class NoticeAttachments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_file_id")
    val noticeFileId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    var notice: Notices,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_files_id", nullable = false)
    var postFile: PostFiles,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "file_role", nullable = false)
    var fileType: FileType,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int

): BaseCreatedTimeEntity() {

}