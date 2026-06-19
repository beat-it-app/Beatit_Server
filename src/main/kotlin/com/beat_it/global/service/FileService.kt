package com.beat_it.global.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

data class FileUploadResult(
    val originalFileName: String,
    val storageKey: String,
    val cdnUrl: String
)

@Service
class FileService {

    fun uploadFiles(
        files: List<MultipartFile>,
        pathPrefix: String = "dummy/path"
    ): List<FileUploadResult> {
        if (files.isEmpty()) return emptyList()

        return files.map { file ->
            // TODO : S3 연동 후 실제 파일 업로드 로직 구현하기
            val originalFileName = file.originalFilename ?: "default_file"
            val storageKey = "$pathPrefix/$originalFileName"
            val cdnUrl = "https://example.com/$originalFileName"

            FileUploadResult(
                originalFileName = originalFileName,
                storageKey = storageKey,
                cdnUrl = cdnUrl
            )
        }
    }
}
