package com.beat_it.team.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.team.dto.TeamCloudFileDetailResponse
import com.beat_it.team.dto.TeamCloudFolderCreateRequest
import com.beat_it.team.dto.TeamCloudFolderUpdateRequest
import com.beat_it.team.dto.TeamCloudItemsDeleteRequest
import com.beat_it.team.dto.TeamCloudItemsMoveRequest
import com.beat_it.team.dto.TeamCloudLinkCreateRequest
import com.beat_it.team.dto.TeamCloudListResponse
import com.beat_it.team.entity.TeamCloudFolders
import com.beat_it.team.entity.TeamCloudItems
import com.beat_it.team.entity.TeamFiles
import com.beat_it.team.entity.Teams
import com.beat_it.team.entity.enum.MediaCategory
import com.beat_it.team.repository.TeamCloudFoldersRepository
import com.beat_it.team.repository.TeamCloudItemsRepository
import com.beat_it.team.repository.TeamFilesRepository
import com.beat_it.team.repository.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class TeamCloudService(
    private val teamCloudFolderRepository: TeamCloudFoldersRepository,
    private val teamRepository: TeamRepository,
    private val teamCloudItemsRepository: TeamCloudItemsRepository,
    private val teamFilesRepository: TeamFilesRepository,
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
            val rootItems = teamCloudItemsRepository.findByTeamAndTeamCloudFolderIsNull(team)

            val folderResponses = rootFolders.map { folder ->
                TeamCloudListResponse.FolderResponse(
                    folderId = folder.teamCloudFolderId!!,
                    folderName = folder.folderName,
                    itemCount = folder.items.size
                )
            }

            val itemResponses = rootItems.map { item ->
                TeamCloudListResponse.ItemResponse(
                    itemId = item.teamCloudItemsId!!,
                    itemName = item.itemName,
                    fileSize = item.teamFiles?.fileSizeBytes,
                    mimeType = item.teamFiles?.mimeType,
                    linkUrl = item.linkUrl,
                    createdAt = DateTimeUtil.format(item.createdAt)
                )
            }

            return TeamCloudListResponse(
                currentFolderName = null,
                folders = folderResponses,
                items = itemResponses
            )
        }

        val targetFolder = validateAndGetFolder(teamId, folderId)
            ?: throw BusinessException(ErrorCode.TEAM_CLOUD_FOLDER_NOT_FOUND)
        val items = teamCloudItemsRepository.findByTeamCloudFolder(targetFolder)

        val itemResponses = items.map { item ->
            TeamCloudListResponse.ItemResponse(
                itemId = item.teamCloudItemsId!!,
                itemName = item.itemName,
                fileSize = item.teamFiles?.fileSizeBytes,
                mimeType = item.teamFiles?.mimeType,
                linkUrl = item.linkUrl,
                createdAt = DateTimeUtil.format(item.createdAt)
            )
        }

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

        val teamFile = item.teamFiles
            ?: throw BusinessException(ErrorCode.TEAM_CLOUD_FILE_NOT_FOUND)

        return TeamCloudFileDetailResponse(
            itemId = item.teamCloudItemsId!!,
            itemName = item.itemName,
            fileSize = teamFile.fileSizeBytes ?: 0L,
            mimeType = teamFile.mimeType ?: "",
            fileUrl = teamFile.cdnUrl ?: ""
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

        val teamFiles = TeamFiles(
            userId = userId,
            originalFileName = fileUploadResult.originalFileName,
            storageKey = fileUploadResult.storageKey,
            cdnUrl = fileUploadResult.cdnUrl,
            mediaCategory = mediaCategory,
            fileSizeBytes = file.size,
            mimeType = file.contentType,
            isPublic = false
        )
        val savedTeamFiles = teamFilesRepository.save(teamFiles)

        val cloudItem = TeamCloudItems(
            team = team,
            uploaderId = userId,
            teamCloudFolder = targetFolder,
            teamFiles = savedTeamFiles,
            linkUrl = null,
            itemName = fileName
        )
        val savedCloudItem = teamCloudItemsRepository.save(cloudItem)

        return savedCloudItem.teamCloudItemsId!!
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

        val cloudItem = TeamCloudItems(
            team = team,
            uploaderId = userId,
            teamCloudFolder = targetFolder,
            teamFiles = null,
            linkUrl = request.linkUrl,
            itemName = request.itemName
        )
        val savedItem = teamCloudItemsRepository.save(cloudItem)

        return savedItem.teamCloudItemsId!!
    }

    @Transactional
    fun moveItems(userId: Long, request: TeamCloudItemsMoveRequest) {
        val teamId = userService.getCurrentTeamId(userId)
        val targetFolder = validateAndGetFolder(teamId, request.targetFolderId)

        val items = teamCloudItemsRepository.findAllById(request.itemIds)

        for (item in items) {
            if (item.team.teamId != teamId) {
                throw BusinessException(ErrorCode.TEAM_CLOUD_ITEM_TEAM_MISMATCH)
            }
            item.changeFolder(targetFolder)
        }
    }

    @Transactional
    fun deleteItems(userId: Long, request: TeamCloudItemsDeleteRequest) {
        val teamId = userService.getCurrentTeamId(userId)

        val items = teamCloudItemsRepository.findAllById(request.itemIds)

        for (item in items) {
            if (item.team.teamId != teamId) {
                throw BusinessException(ErrorCode.TEAM_CLOUD_ITEM_TEAM_MISMATCH)
            }
        }

        val storageKeys = items.mapNotNull { it.teamFiles?.storageKey }
        if (storageKeys.isNotEmpty()) {
            fileService.deleteFiles(storageKeys)
        }

        teamCloudItemsRepository.deleteAll(items)
    }



    @Transactional
    fun createFolder(userId: Long, request: TeamCloudFolderCreateRequest): Long {
        val teamId = userService.getCurrentTeamId(userId)
        val team = findTeamOrThrow(teamId)

        val folder = TeamCloudFolders(
            team = team,
            folderName = request.folderName,
            creatorId = userId
        )

        val savedFolder = teamCloudFolderRepository.save(folder)
        return savedFolder.teamCloudFolderId!!
    }

    @Transactional
    fun updateFolder(userId: Long, folderId: Long, request: TeamCloudFolderUpdateRequest) {
        val teamId = userService.getCurrentTeamId(userId)
        val folder = findFolderOrThrow(folderId)
        validateFolderTeam(folder, teamId)

        folder.updateFolderName(request.folderName)
    }

    @Transactional
    fun deleteFolder(userId: Long, folderId: Long) {
        val teamId = userService.getCurrentTeamId(userId)

        val folder = findFolderOrThrow(folderId)
        validateFolderTeam(folder, teamId)

        val itemsInFolder = teamCloudItemsRepository.findByTeamCloudFolder(folder)

        val storageKeys = itemsInFolder.mapNotNull { it.teamFiles?.storageKey }
        if (storageKeys.isNotEmpty()) {
            fileService.deleteFiles(storageKeys)
        }

        teamCloudFolderRepository.delete(folder)
    }

    private fun validateStorageLimit(team: Teams, newFileSize: Long) {
        // TODO: [요금제 및 동시성 고려] 비관락 or 분산락 적용 필요
        val items = teamCloudItemsRepository.findByTeam(team)
        val currentTotalBytes = items.sumOf { item -> item.teamFiles?.fileSizeBytes ?: 0L }

        if (currentTotalBytes + newFileSize > MAX_STORAGE_BYTES) {
            throw BusinessException(ErrorCode.TEAM_CLOUD_STORAGE_EXCEEDED)
        }
    }

    private fun findTeamOrThrow(teamId: Long): Teams {
        return teamRepository.findById(teamId)
            .orElseThrow { BusinessException(ErrorCode.TEAM_UNAVAILABLE) }
    }

    private fun findFolderOrThrow(folderId: Long): TeamCloudFolders {
        return teamCloudFolderRepository.findById(folderId)
            .orElseThrow { BusinessException(ErrorCode.TEAM_CLOUD_FOLDER_NOT_FOUND) }
    }

    private fun findItemOrThrow(itemId: Long): TeamCloudItems {
        return teamCloudItemsRepository.findById(itemId)
            .orElseThrow { BusinessException(ErrorCode.TEAM_CLOUD_ITEM_NOT_FOUND) }
    }

    private fun validateFolderTeam(folder: TeamCloudFolders, teamId: Long) {
        if (folder.team.teamId != teamId) {
            throw BusinessException(ErrorCode.TEAM_CLOUD_FOLDER_TEAM_MISMATCH)
        }
    }

    private fun validateAndGetFolder(teamId: Long, folderId: Long?): TeamCloudFolders? {
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
}