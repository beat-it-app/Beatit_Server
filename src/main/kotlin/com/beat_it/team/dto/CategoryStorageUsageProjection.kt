package com.beat_it.team.dto

import com.beat_it.team.entity.enum.MediaCategory

interface CategoryStorageUsageProjection {
    val category: MediaCategory
    val totalBytes: Long
}