package com.beat_it.team.service

import com.beat_it.auth.dto.UserProfileResponse
import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import com.beat_it.global.service.FileUploadResult
import com.beat_it.team.dto.TeamCloudFileDetailResponse
import com.beat_it.team.dto.TeamCloudFolderRequest
import com.beat_it.team.dto.TeamCloudItemsDeleteRequest
import com.beat_it.team.dto.TeamCloudItemsMoveRequest
import com.beat_it.team.dto.TeamCloudLinkCreateRequest
import com.beat_it.team.dto.TeamCloudListResponse
import com.beat_it.team.entity.TeamCloudFolder
import com.beat_it.team.entity.TeamCloudItem
import com.beat_it.team.entity.TeamFile
import com.beat_it.team.entity.Teams
import com.beat_it.team.entity.enum.MediaCategory
import com.beat_it.team.repository.TeamCloudFolderRepository
import com.beat_it.team.repository.TeamCloudItemRepository
import com.beat_it.team.repository.TeamFileRepository
import com.beat_it.team.repository.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class TeamCloudService(
    private val teamCloudFolderRepository: TeamCloudFolderRepository,
    private val teamRepository: TeamRepository,
    private val teamCloudItemRepository: TeamCloudItemRepository,
    private val teamFileRepository: TeamFileRepository,
    private val fileService: FileService,
    private val userService: UserService
) {
    companion object {
        private const val MAX_STORAGE_BYTES: Long = 10L * 1024 * 1024 * 1024
    }

    @Transactional(readOnly = true)
    fun getTeamCloudList(userId: Long, folderId: Long?): TeamCloudListResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val team = findTeamOrThrow(teamId)

        if (folderId == null) {
            val rootFolders = teamCloudFolderRepository.findByTeam(team)
            val rootItems = teamCloudItemRepository.findByTeamAndTeamCloudFolderIsNull(team)

            val creatorIds = rootFolders.map { it.creatorId }
            val uploaderIds = rootItems.map { it.uploaderId }
            val allUserIds = (creatorIds + uploaderIds).distinct()

            val userProfileMap = userService.getUserProfiles(allUserIds).associateBy { it.userId }

            val folderResponses = rootFolders.map { folder ->
                val creatorProfile = userProfileMap[folder.creatorId]
                TeamCloudListResponse.FolderResponse(
                    folderId = folder.teamCloudFolderId!!,
                    folderName = folder.folderName,
                    itemCount = folder.items.size,
                    creatorName = creatorProfile?.name ?: "알 수 없음"
                )
            }

            val itemResponses = mapToItemResponses(rootItems, userProfileMap)


            return TeamCloudListResponse(
                currentFolderName = null,
                folders = folderResponses,
                items = itemResponses
            )
        }

        val targetFolder = validateAndGetFolder(teamId, folderId)
            ?: throw BusinessException(ErrorCode.TEAM_CLOUD_FOLDER_NOT_FOUND)
        val items = teamCloudItemRepository.findByTeamCloudFolder(targetFolder)

        val uploaderIds = items.map { it.uploaderId }.distinct()
        val userProfileMap = userService.getUserProfiles(uploaderIds).associateBy { it.userId }

        val itemResponses = mapToItemResponses(items, userProfileMap)

        return TeamCloudListResponse(
            currentFolderName = targetFolder.folderName,
            folders = emptyList(),
            items = itemResponses
        )
    }

    @Transactional(readOnly = true)
    fun getTeamCloudFileDetail(userId: Long, itemId: Long): TeamCloudFileDetailResponse {
        val teamId = userService.getCurrentTeamId(userId)

        val item = findItemOrThrow(itemId)
        if (item.team.teamId != teamId) {
            throw BusinessException(ErrorCode.TEAM_CLOUD_ITEM_TEAM_MISMATCH)
        }

        val teamFile = item.teamFile
            ?: throw BusinessException(ErrorCode.TEAM_CLOUD_FILE_NOT_FOUND)

        return TeamCloudFileDetailResponse(
            itemId = itemId,
            itemName = item.itemName,
            fileSize = teamFile.fileSizeBytes ?: 0L,
            mimeType = teamFile.mimeType ?: "",
            fileUrl = teamFile.cdnUrl
        )
    }

    @Transactional
    fun uploadTeamCloudFile(
        userId: Long,
        folderId: Long?,
        file: MultipartFile,
        fileName: String
    ): Long {
        val teamId = userService.getCurrentTeamId(userId)
        val team = findTeamOrThrow(teamId)

        validateStorageLimit(team, file.size)

        val targetFolder = validateAndGetFolder(teamId, folderId)

        val fileUploadResult = fileService.uploadFile(file, "team-clouds/$teamId")
        val mediaCategory = determineMediaCategory(fileUploadResult.originalFileName)

        try {
            return saveTeamCloudFileToDatabase(
                team = team,
                userId = userId,
                targetFolder = targetFolder,
                fileUploadResult = fileUploadResult,
                mediaCategory = mediaCategory,
                fileSize = file.size,
                contentType = file.contentType,
                itemName = fileName
            )
        } catch (e: Exception) {
            fileService.deleteFile(fileUploadResult.storageKey)
            throw BusinessException(ErrorCode.TEAM_CLOUD_FILE_UPLOAD_FAILED)
        }
    }

    @Transactional
    protected fun saveTeamCloudFileToDatabase(
        team: Teams,
        userId: Long,
        targetFolder: TeamCloudFolder?,
        fileUploadResult: FileUploadResult,
        mediaCategory: MediaCategory,
        fileSize: Long,
        contentType: String?,
        itemName: String
    ): Long {
        val teamFile = TeamFile(
            userId = userId,
            originalFileName = fileUploadResult.originalFileName,
            storageKey = fileUploadResult.storageKey,
            cdnUrl = fileUploadResult.cdnUrl,
            mediaCategory = mediaCategory,
            fileSizeBytes = fileSize,
            mimeType = contentType,
            isPublic = false
        )
        val savedTeamFile = teamFileRepository.save(teamFile)

        val cloudItem = TeamCloudItem(
            team = team,
            uploaderId = userId,
            teamCloudFolder = targetFolder,
            teamFile = savedTeamFile,
            linkUrl = null,
            itemName = itemName
        )
        val savedCloudItem = teamCloudItemRepository.save(cloudItem)

        return savedCloudItem.teamCloudItemId!!
    }

    @Transactional
    fun createTeamCloudLink(
        userId: Long,
        folderId: Long?,
        request: TeamCloudLinkCreateRequest
    ): Long {
        val teamId = userService.getCurrentTeamId(userId)
        val team = findTeamOrThrow(teamId)
        val targetFolder = validateAndGetFolder(teamId, folderId)

        val cloudItem = TeamCloudItem(
            team = team,
            uploaderId = userId,
            teamCloudFolder = targetFolder,
            teamFile = null,
            linkUrl = request.linkUrl,
            itemName = request.itemName
        )
        val savedItem = teamCloudItemRepository.save(cloudItem)

        return savedItem.teamCloudItemId!!
    }

    @Transactional
    fun moveItems(userId: Long, request: TeamCloudItemsMoveRequest) {
        val teamId = userService.getCurrentTeamId(userId)
        val targetFolder = validateAndGetFolder(teamId, request.targetFolderId)

        val items = teamCloudItemRepository.findAllById(request.itemIds)

        for (item in items) {
            if (item.team.teamId != teamId) {
                throw BusinessException(ErrorCode.TEAM_CLOUD_ITEM_TEAM_MISMATCH)
            }
            item.changeFolder(targetFolder)
        }
    }

    fun deleteItems(userId: Long, request: TeamCloudItemsDeleteRequest) {
        val teamId = userService.getCurrentTeamId(userId)

        val items = teamCloudItemRepository.findAllById(request.itemIds)

        for (item in items) {
            if (item.team.teamId != teamId) {
                throw BusinessException(ErrorCode.TEAM_CLOUD_ITEM_TEAM_MISMATCH)
            }
        }

        val storageKeys = items.mapNotNull { item ->
            item.teamFile?.storageKey
        }

        if (storageKeys.isNotEmpty()) {
            fileService.deleteFiles(storageKeys)
        }

        deleteItemsFromDatabase(items)
    }

    @Transactional
    protected fun deleteItemsFromDatabase(items: List<TeamCloudItem>) {
        teamCloudItemRepository.deleteAll(items)
    }



    @Transactional
    fun createFolder(userId: Long, request: TeamCloudFolderRequest): Long {
        val teamId = userService.getCurrentTeamId(userId)
        val team = findTeamOrThrow(teamId)

        validateDuplicateFolderName(team, request.folderName)

        val folder = TeamCloudFolder(
            team = team,
            folderName = request.folderName,
            creatorId = userId
        )

        val savedFolder = teamCloudFolderRepository.save(folder)
        return savedFolder.teamCloudFolderId!!
    }

    @Transactional
    fun updateFolder(userId: Long, folderId: Long, request: TeamCloudFolderRequest) {
        val teamId = userService.getCurrentTeamId(userId)
        val folder = findFolderOrThrow(folderId)
        validateFolderTeam(folder, teamId)
        validateDuplicateFolderName(folder.team, request.folderName, folder.folderName)

        folder.updateFolderName(request.folderName)
    }

    @Transactional
    fun deleteFolder(userId: Long, folderId: Long) {
        val teamId = userService.getCurrentTeamId(userId)

        val folder = findFolderOrThrow(folderId)
        validateFolderTeam(folder, teamId)

        val itemsInFolder = teamCloudItemRepository.findByTeamCloudFolder(folder)

        val storageKeys = itemsInFolder.mapNotNull { it.teamFile?.storageKey }
        if (storageKeys.isNotEmpty()) {
            fileService.deleteFiles(storageKeys)
        }

        teamCloudFolderRepository.delete(folder)
    }

    private fun validateStorageLimit(team: Teams, newFileSize: Long) {
        // TODO: [요금제 및 동시성 고려] 비관락 or 분산락 적용 필요
        val items = teamCloudItemRepository.findByTeam(team)
        val currentTotalBytes = items.sumOf { item -> item.teamFile?.fileSizeBytes ?: 0L }

        if (currentTotalBytes + newFileSize > MAX_STORAGE_BYTES) {
            throw BusinessException(ErrorCode.TEAM_CLOUD_STORAGE_EXCEEDED)
        }
    }

    private fun findTeamOrThrow(teamId: Long): Teams {
        return teamRepository.findById(teamId)
            .orElseThrow { BusinessException(ErrorCode.TEAM_UNAVAILABLE) }
    }

    private fun findFolderOrThrow(folderId: Long): TeamCloudFolder {
        return teamCloudFolderRepository.findById(folderId)
            .orElseThrow { BusinessException(ErrorCode.TEAM_CLOUD_FOLDER_NOT_FOUND) }
    }

    private fun findItemOrThrow(itemId: Long): TeamCloudItem {
        return teamCloudItemRepository.findById(itemId)
            .orElseThrow { BusinessException(ErrorCode.TEAM_CLOUD_ITEM_NOT_FOUND) }
    }

    private fun validateFolderTeam(folder: TeamCloudFolder, teamId: Long) {
        if (folder.team.teamId != teamId) {
            throw BusinessException(ErrorCode.TEAM_CLOUD_FOLDER_TEAM_MISMATCH)
        }
    }

    private fun validateAndGetFolder(teamId: Long, folderId: Long?): TeamCloudFolder? {
        return folderId?.let { id ->
            val folder = findFolderOrThrow(id)
            validateFolderTeam(folder, teamId)
            folder
        }
    }

    private fun determineMediaCategory(fileName: String): MediaCategory {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "jpg", "jpeg", "png" -> MediaCategory.IMAGE
            "mp3", "wav" -> MediaCategory.AUDIO
            "mp4", "avi", "mov" -> MediaCategory.VIDEO
            else -> MediaCategory.DOCUMENT
        }
    }

    private fun validateDuplicateFolderName(team: Teams, newFolderName: String, currentFolderName: String? = null) {
        if (currentFolderName != null && currentFolderName == newFolderName) {
            return
        }
        if (teamCloudFolderRepository.existsByTeamAndFolderName(team, newFolderName)) {
            throw BusinessException(ErrorCode.TEAM_CLOUD_FOLDER_ALREADY_EXISTS)
        }
    }

    private fun mapToItemResponses(
        items: List<TeamCloudItem>,
        userProfileMap: Map<Long, UserProfileResponse>
    ): List<TeamCloudListResponse.ItemResponse> {
        return items.map { item ->
            val uploaderProfile = userProfileMap[item.uploaderId]
            TeamCloudListResponse.ItemResponse(
                itemId = item.teamCloudItemId!!,
                itemName = item.itemName,
                fileSize = item.teamFile?.fileSizeBytes,
                mimeType = item.teamFile?.mimeType,
                linkUrl = item.linkUrl,
                uploaderName = uploaderProfile?.name ?: "알 수 없음",
                createdAt = item.createdAt
            )
        }
    }
}