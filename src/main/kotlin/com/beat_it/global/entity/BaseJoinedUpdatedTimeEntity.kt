package com.beat_it.global.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseJoinedUpdatedTimeEntity {
    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    var joinedAt: OffsetDateTime = OffsetDateTime.now()
        protected set

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
        protected set

    fun updateTimestamp() {
        this.updatedAt = OffsetDateTime.now()
    }
}