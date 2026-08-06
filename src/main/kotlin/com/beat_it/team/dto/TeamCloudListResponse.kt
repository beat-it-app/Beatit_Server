package com.beat_it.team.dto

data class TeamCloudListResponse(
    val currentFolderName: String?,
    val folders: List<FolderResponse>,
    val items: List<ItemResponse>
) {
    data class FolderResponse(
        val folderId: Long,
        val folderName: String,
        val itemCount: Int
    )

    data class ItemResponse(
        val itemId: Long,
        val itemName: String,
        val fileSize: Long?,
        val mimeType: String?,
        val linkUrl: String?,
        val createdAt: String
    )
}