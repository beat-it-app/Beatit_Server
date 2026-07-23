package com.beat_it.global.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.OffsetDateTime

@MappedSuperclass
abstract class BaseJoinedUpdateEntity {
    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    var joinedAt: OffsetDateTime = OffsetDateTime.now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
        protected set
}