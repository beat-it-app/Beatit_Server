package com.beat_it.global.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.OffsetDateTime

@MappedSuperclass
class BaseDeletedTimeEntity : BaseUpdatedTimeEntity() {
    @Column(name = "deleted_at", nullable = true)
    var deletedAt: OffsetDateTime? = null
        protected set

    fun delete() {
        this.deletedAt = OffsetDateTime.now()
    }
}