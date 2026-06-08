package com.beat_it.post.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.post.entity.enum.PostType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "notice_comment")
class PostComments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    val commentId: Long? = null,

    @Column(name = "post_type")
    val postType: PostType,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    var content: String,

): BaseUpdatedTimeEntity() {
}