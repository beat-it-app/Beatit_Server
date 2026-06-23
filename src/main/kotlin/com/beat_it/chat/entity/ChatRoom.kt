package com.beat_it.chat.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "chat_rooms")
class ChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    val id: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Column(name = "team_id", nullable = false)
    val teamId: Long,

    @Column(name = "title", nullable = false, length = 100)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: ChatRoomType,

    @Column(name = "last_message_at")
    var lastMessageAt: OffsetDateTime? = null,

    @Column(name = "last_message_content", length = 255)
    var lastMessageContent: String? = null,

) : BaseUpdatedTimeEntity() {

    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<ChatMember> = mutableListOf()
}