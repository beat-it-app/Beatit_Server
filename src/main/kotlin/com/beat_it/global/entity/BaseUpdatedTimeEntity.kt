package com.beat_it.global.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.OffsetDateTime

@MappedSuperclass
abstract class BaseUpdatedTimeEntity : BaseCreatedTimeEntity() {
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
        protected set

    fun updateTimestamp() {
        this.updatedAt = OffsetDateTime.now()
    }
}