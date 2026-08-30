package com.beat_it.global.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

data class FileUploadResult(
    val originalFileName: String,
    val storageKey: String,
    val cdnUrl: String
)

@Service
class FileService {
    val allowedExtensions = setOf("jpg", "jpeg", "png", "mp3", "wav")
    val maxFileSize = 10 * 1024 * 1024 // 10MB를 바이트 단위로 계산

    @Transactional
    fun uploadFile(
        file: MultipartFile,
        pathPrefix: String = "dummy/path"
    ): FileUploadResult {
        if (file.isEmpty) {
            throw BusinessException(ErrorCode.EMPTY_FILE)
        }

        val originalFileName = file.originalFilename ?: "default_file"
        val extension = originalFileName.substringAfterLast(".", "").lowercase()
        
        if (!allowedExtensions.contains(extension)) {
            throw BusinessException(ErrorCode.INVALID_FILE_EXTENSION)
        }
        
        if (file.size > maxFileSize) {
            throw BusinessException(ErrorCode.FILE_SIZE_EXCEEDED)
        }

        val storageKey = "$pathPrefix/${System.currentTimeMillis()}_$originalFileName"
        val cdnUrl = "https://example.com/$storageKey"

        return FileUploadResult(
            originalFileName = originalFileName,
            storageKey = storageKey,
            cdnUrl = cdnUrl
        )
    }

    @Transactional
    fun uploadFiles(
        files: List<MultipartFile>,
        pathPrefix: String = "dummy/path"
    ): List<FileUploadResult> {
        return files.map { uploadFile(it, pathPrefix) }
    }

    @Transactional
    fun deleteFile(storageKey: String) {
        // TODO: S3 실제 삭제 로직 구현
        println("Deleting file from storage: $storageKey")
    }

    @Transactional
    fun deleteFiles(storageKeys: List<String>) {
        for (storageKey in storageKeys) {
            deleteFile(storageKey)
        }
    }
}
