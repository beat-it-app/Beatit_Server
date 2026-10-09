package com.beat_it.team.dto

data class ArchiveUpdateRequest(
    val title: String? = null,
    val locationId: Long? = null,
    val description: String? = null,
    // null: 기존 수정 API 동작 유지, []: 모두 삭제, 값 지정: 해당 URL만 보존
    val retainArchiveImageUrls: List<String>? = null
)

