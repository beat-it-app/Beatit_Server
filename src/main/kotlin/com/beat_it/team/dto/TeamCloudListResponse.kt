package com.beat_it.team.dto

import java.time.OffsetDateTime

data class TeamCloudListResponse(
    val currentFolderName: String?,
    val folders: List<FolderResponse>,
    val items: List<ItemResponse>
) {
    data class FolderResponse(
        val folderId: Long,
        val folderName: String,
        val itemCount: Int,
        val creatorName: String
    )

    data class ItemResponse(
        val itemId: Long,
        val itemName: String,
        val fileSize: Long?,
        val mimeType: String?,
        val linkUrl: String?,
        val uploaderName: String,
        val createdAt: OffsetDateTime
    )
}