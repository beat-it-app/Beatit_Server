package com.beat_it.global.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseJoinedTimeEntity {
    @CreatedDate
    @Column(name = "joined_at", updatable = false, nullable = false)
    var joinedAt: OffsetDateTime = OffsetDateTime.now()
        protected set
}