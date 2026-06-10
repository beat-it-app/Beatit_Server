package com.beat_it.post.service

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.post.entity.PostFiles
import com.beat_it.post.repository.PostFilesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class PostFileService(
    private val postFilesRepository: PostFilesRepository
) {

    @Transactional
    fun uploadFiles(
        userId: Long,
        files: List<MultipartFile>,
        mediaCategory: MediaCategory = MediaCategory.IMAGE
    ): List<PostFiles> {
        if (files.isEmpty()) return emptyList()

        return files.map { file ->
            // TODO : S3 연동 후 실제 파일 업로드 로직 구현하기
            val originalFileName = file.originalFilename ?: "default_file"
            val storageKey = "dummy/path/$originalFileName"
            val cdnUrl = "https://example.com/$originalFileName"

            val postFile = PostFiles(
                userId = userId,
                originalFileName = originalFileName,
                storageKey = storageKey,
                cdnUrl = cdnUrl,
                mediaCategory = mediaCategory,
                isPublic = true
            )
            postFilesRepository.save(postFile)
        }
    }
}
