package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole
import java.time.OffsetDateTime

data class TeamCloudListResponse(
    val currentFolderName: String?,
    val currentUserRole: TeamRole,
    val folders: List<FolderResponse>,
    val items: List<ItemResponse>
) {
    data class FolderResponse(
        val folderId: Long,
        val folderName: String,
        val itemCount: Int,
        val creatorId: Long,
        val creatorName: String,
        val isWriter: Boolean
    )

    data class ItemResponse(
        val itemId: Long,
        val itemName: String,
        val fileSize: Long?,
        val mimeType: String?,
        val linkUrl: String?,
        val uploaderId: Long,
        val uploaderName: String,
        val isWriter: Boolean,
        val createdAt: OffsetDateTime
    )
}