package com.beat_it.team.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileDirectory
import com.beat_it.global.service.FileService
import com.beat_it.global.service.FileUploadResult
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class TeamImageUploadService(private val fileService: FileService) {
    fun uploadImage(file: MultipartFile): FileUploadResult {
        validate(file)
        return fileService.uploadFile(file, FileDirectory.TEAM)
    }

    fun uploadImages(files: List<MultipartFile>): List<FileUploadResult> {
        files.forEach(::validate)
        val uploaded = mutableListOf<FileUploadResult>()
        try {
            files.forEach { uploaded.add(fileService.uploadFile(it, FileDirectory.TEAM)) }
            return uploaded
        } catch (e: Exception) {
            if (uploaded.isNotEmpty()) runCatching { fileService.deleteFiles(uploaded.map { it.storageKey }) }
            throw e
        }
    }

    private fun validate(file: MultipartFile) {
        if (file.isEmpty) throw BusinessException(ErrorCode.EMPTY_FILE)
        val extension = file.originalFilename?.substringAfterLast('.', "")?.lowercase() ?: ""
        if (extension !in setOf("jpg", "jpeg", "png", "gif", "webp", "heic") ||
            file.contentType?.lowercase()?.startsWith("image/") != true) {
            throw BusinessException(ErrorCode.INVALID_FILE_EXTENSION)
        }
    }
}
