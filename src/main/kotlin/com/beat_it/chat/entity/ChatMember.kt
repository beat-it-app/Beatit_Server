package com.beat_it.chat.entity

import com.beat_it.global.entity.BaseJoinedTimeEntity
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "chat_members")
class ChatMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_member_id")
    val chatMemberId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chatRoom: ChatRoom,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "last_chat_message_id")
    var lastChatMessageId: Long? = null,

    @Column(name = "is_muted", nullable = false)
    var isMuted: Boolean = false,
) : BaseJoinedTimeEntity()