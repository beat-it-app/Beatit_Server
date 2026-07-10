package com.beat_it.chat.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    val chatMessageId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chatRoom: ChatRoom,

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    val type: ChatMessageType,

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "shared_user_id")
    val sharedUserId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val senderId: Long

) : BaseCreatedTimeEntity()