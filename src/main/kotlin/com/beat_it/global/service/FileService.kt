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
    fun uploadFiles(
        files: List<MultipartFile>,
        pathPrefix: String = "dummy/path"
    ): List<FileUploadResult> {
        if (files.isEmpty()) return emptyList()

        if (files.any { it.isEmpty }) {
            throw BusinessException(ErrorCode.EMPTY_FILE)
        }

        return files.map { file ->
            val originalFileName = file.originalFilename ?: "default_file"

            val extension = originalFileName.substringAfterLast(".", "").lowercase()
            if (!allowedExtensions.contains(extension)) {
                throw BusinessException(ErrorCode.INVALID_FILE_EXTENSION)
            }
            
            if (file.size > maxFileSize) {
                throw BusinessException(ErrorCode.FILE_SIZE_EXCEEDED)
            }

            val storageKey = "$pathPrefix/$originalFileName"
            val cdnUrl = "https://example.com/$originalFileName"

            try {
                // TODO : S3 연동 후 실제 파일 업로드 로직 구현하기
                FileUploadResult(
                    originalFileName = originalFileName,
                    storageKey = storageKey,
                    cdnUrl = cdnUrl
                )
            } catch (e: Exception) {
                throw BusinessException(ErrorCode.FILE_UPLOAD_FAILED)
            }
        }
    }
}
