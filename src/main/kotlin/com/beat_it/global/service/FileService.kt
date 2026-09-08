package com.beat_it.global.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

data class FileUploadResult(
    val originalFileName: String,
    val storageKey: String,
    val cdnUrl: String
)

data class PresignedUrlResponse(
    val presignedUrl: String,
    val storageKey: String,
    val cdnUrl: String,
    val expirationMinutes: Long
)

@Service
class FileService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${cloud.aws.cloudfront.domain}") private val cloudFrontDomain: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    val allowedExtensions = setOf(
        "jpg", "jpeg", "png", "gif", "webp", "heic",
        "mp3", "wav", "m4a", "aac", "ogg", "flac",
        "mp4", "mov", "avi",
        "pdf", "zip", "hwp", "docx"
    )
    val maxFileSize = 50 * 1024 * 1024L // 50MB

    /**
     * file(직접 업로드) 또는 storageKey(Presigned URL로 사전 업로드) 중 하나를 받아 FileUploadResult 생성
     */
    fun resolveFile(
        file: MultipartFile?,
        storageKey: String?,
        directory: FileDirectory,
        originalFileName: String? = null
    ): FileUploadResult? {
        val hasFile = file != null && !file.isEmpty
        val hasKey = !storageKey.isNullOrBlank()

        if (!hasFile && !hasKey) {
            return null
        }

        if (hasFile && hasKey) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        if (hasKey) {
            val key = storageKey!!.trim()
            val resolvedOriginalName = originalFileName?.takeIf { it.isNotBlank() }
                ?: key.substringAfterLast("/").substringAfter("_")
            val cdnUrl = "https://$cloudFrontDomain/$key"

            return FileUploadResult(
                originalFileName = resolvedOriginalName,
                storageKey = key,
                cdnUrl = cdnUrl
            )
        }

        return uploadFile(file!!, directory)
    }

    /**
     * 다중 파일 및 다중 storageKey를 모두 취합하여 List<FileUploadResult> 생성
     */
    fun resolveFiles(
        files: List<MultipartFile>?,
        storageKeys: List<String>?,
        directory: FileDirectory
    ): List<FileUploadResult> {
        val results = mutableListOf<FileUploadResult>()

        if (!files.isNullOrEmpty()) {
            val validFiles = files.filter { !it.isEmpty }
            if (validFiles.isNotEmpty()) {
                results.addAll(uploadFiles(validFiles, directory))
            }
        }

        if (!storageKeys.isNullOrEmpty()) {
            val validKeys = storageKeys.filter { it.isNotBlank() }.map { it.trim() }
            for (key in validKeys) {
                val resolvedOriginalName = key.substringAfterLast("/").substringAfter("_")
                val cdnUrl = "https://$cloudFrontDomain/$key"
                results.add(
                    FileUploadResult(
                        originalFileName = resolvedOriginalName,
                        storageKey = key,
                        cdnUrl = cdnUrl
                    )
                )
            }
        }

        return results
    }

    fun uploadFile(
        file: MultipartFile,
        directory: FileDirectory
    ): FileUploadResult {
        return uploadFile(file, directory.path)
    }

    fun uploadFiles(
        files: List<MultipartFile>,
        directory: FileDirectory
    ): List<FileUploadResult> {
        return uploadFiles(files, directory.path)
    }

    fun uploadFile(
        file: MultipartFile,
        pathPrefix: String = "common"
    ): FileUploadResult {
        if (file.isEmpty) {
            throw BusinessException(ErrorCode.EMPTY_FILE)
        }

        val originalFileName = file.originalFilename ?: "unknown_file"
        val extension = originalFileName.substringAfterLast(".", "").lowercase()

        if (!allowedExtensions.contains(extension)) {
            log.warn("Invalid file extension: $extension (file: $originalFileName)")
            throw BusinessException(ErrorCode.INVALID_FILE_EXTENSION)
        }

        if (file.size > maxFileSize) {
            log.warn("File size exceeded: ${file.size} bytes > $maxFileSize bytes")
            throw BusinessException(ErrorCode.FILE_SIZE_EXCEEDED)
        }

        val cleanPath = pathPrefix.trim().trim('/')
        val sanitizedOriginalName = originalFileName.replace("[^a-zA-Z0-9가-힣._-]".toRegex(), "_")
        val uniqueFileName = "${UUID.randomUUID()}_$sanitizedOriginalName"
        val storageKey = if (cleanPath.isBlank()) uniqueFileName else "$cleanPath/$uniqueFileName"

        try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(file.contentType ?: "application/octet-stream")
                .contentLength(file.size)
                .build()

            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.inputStream, file.size)
            )

            val cdnUrl = "https://$cloudFrontDomain/$storageKey"

            log.info("S3 file uploaded successfully: key=$storageKey, url=$cdnUrl")

            return FileUploadResult(
                originalFileName = originalFileName,
                storageKey = storageKey,
                cdnUrl = cdnUrl
            )
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to upload file to S3: originalFileName=$originalFileName, key=$storageKey", e)
            throw BusinessException(ErrorCode.FILE_UPLOAD_FAILED)
        }
    }

    fun uploadFiles(
        files: List<MultipartFile>,
        pathPrefix: String = "common"
    ): List<FileUploadResult> {
        return files.map { uploadFile(it, pathPrefix) }
    }

    fun generatePresignedUploadUrl(
        originalFileName: String,
        directory: FileDirectory = FileDirectory.COMMON,
        contentType: String? = null
    ): PresignedUrlResponse {
        if (originalFileName.isBlank()) {
            throw BusinessException(ErrorCode.EMPTY_FILE)
        }

        val extension = originalFileName.substringAfterLast(".", "").lowercase()
        if (!allowedExtensions.contains(extension)) {
            log.warn("Invalid file extension for presigned url: $extension (file: $originalFileName)")
            throw BusinessException(ErrorCode.INVALID_FILE_EXTENSION)
        }

        val cleanPath = directory.path.trim().trim('/')
        val sanitizedOriginalName = originalFileName.replace("[^a-zA-Z0-9가-힣._-]".toRegex(), "_")
        val uniqueFileName = "${UUID.randomUUID()}_$sanitizedOriginalName"
        val storageKey = if (cleanPath.isBlank()) uniqueFileName else "$cleanPath/$uniqueFileName"

        // 확장자 기반 Content-Type 자동 감지 (직접 전달된 값이 없으면 자동 유추)
        val resolvedContentType = contentType?.takeIf { it.isNotBlank() }
            ?: org.springframework.http.MediaTypeFactory.getMediaType(originalFileName)
                .map { it.toString() }
                .orElse("application/octet-stream")

        val expirationMinutes = 10L

        try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(resolvedContentType)
                .build()

            val putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(putObjectRequest)
                .build()

            val presignedPutObjectRequest = s3Presigner.presignPutObject(putObjectPresignRequest)
            val presignedUrl = presignedPutObjectRequest.url().toExternalForm()
            val cdnUrl = "https://$cloudFrontDomain/$storageKey"

            log.info("Generated S3 Presigned URL: key=$storageKey, contentType=$resolvedContentType, url=$presignedUrl")

            return PresignedUrlResponse(
                presignedUrl = presignedUrl,
                storageKey = storageKey,
                cdnUrl = cdnUrl,
                expirationMinutes = expirationMinutes
            )
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to generate presigned upload url: originalFileName=$originalFileName", e)
            throw BusinessException(ErrorCode.FILE_UPLOAD_FAILED)
        }
    }

    fun deleteFile(storageKey: String) {
        if (storageKey.isBlank()) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val key = storageKey.trim()

        try {
            val deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()

            s3Client.deleteObject(deleteObjectRequest)
            log.info("S3 file deleted successfully: key=$key")
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to delete file from S3: key=$key", e)
            throw BusinessException(ErrorCode.FILE_DELETE_FAILED)
        }
    }

    fun deleteFiles(storageKeys: List<String>) {
        if (storageKeys.isEmpty()) return

        val validKeys = storageKeys
            .filter { it.isNotBlank() }
            .map { it.trim() }

        if (validKeys.isEmpty()) return

        try {
            val objectIdentifiers = validKeys.map { key ->
                ObjectIdentifier.builder()
                    .key(key)
                    .build()
            }

            val deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(
                    Delete.builder()
                        .objects(objectIdentifiers)
                        .build()
                )
                .build()

            val response = s3Client.deleteObjects(deleteObjectsRequest)

            if (response.hasErrors() && response.errors().isNotEmpty()) {
                val errorMessages = response.errors().joinToString { "${it.key()}: ${it.message()}" }
                log.error("Failed to delete some files from S3: $errorMessages")
                throw BusinessException(ErrorCode.FILE_DELETE_FAILED)
            }

            log.info("S3 files deleted successfully: count=${validKeys.size}")
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to delete bulk files from S3: keys=$validKeys", e)
            throw BusinessException(ErrorCode.FILE_DELETE_FAILED)
        }
    }

    /**
     * storageKey를 CloudFront CDN URL로 변환
     */
    fun getFileUrl(storageKey: String): String {
        return "https://$cloudFrontDomain/$storageKey"
    }
}
