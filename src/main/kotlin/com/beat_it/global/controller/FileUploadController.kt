package com.beat_it.global.controller

import com.beat_it.global.response.BasicResponse
import com.beat_it.global.service.FileDirectory
import com.beat_it.global.service.FileService
import com.beat_it.global.service.FileUploadResult
import com.beat_it.global.service.PresignedUrlResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "8-2. (GLOBAL) FILE API", description = "파일 업로드 및 삭제 API")
@RestController
@RequestMapping("/files")
class FileUploadController(
    private val fileService: FileService
) {

    @Operation(
        summary = "S3 Presigned URL 발급 (클라이언트 직접 업로드용)",
        description = "클라이언트에서 S3로 직접 대용량/미디어 파일을 업로드할 수 있는 1회용 사전 서명된 PUT URL을 발급합니다."
    )
    @GetMapping("/presigned-url")
    fun getPresignedUploadUrl(
        @RequestParam originalFileName: String,
        @RequestParam(defaultValue = "COMMON") directory: FileDirectory,
        @RequestParam(required = false) contentType: String?,
        @RequestParam(defaultValue = "10") expirationMinutes: Long
    ): ResponseEntity<BasicResponse<PresignedUrlResponse>> {
        val result = fileService.generatePresignedUploadUrl(
            originalFileName = originalFileName,
            directory = directory,
            contentType = contentType,
            expirationMinutes = expirationMinutes
        )
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(result, HttpStatus.OK, "Presigned URL 발급에 성공했습니다."))
    }

    @Operation(summary = "단일 파일 업로드 (서버 경유)", description = "S3 버킷의 지정된 디렉토리에 단일 파일을 서버를 통해 업로드합니다.")
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestPart("file") file: MultipartFile,
        @RequestParam(defaultValue = "COMMON") directory: FileDirectory
    ): ResponseEntity<BasicResponse<FileUploadResult>> {
        val result = fileService.uploadFile(file, directory)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(result, HttpStatus.OK, "파일 업로드에 성공했습니다."))
    }

    @Operation(summary = "다중 파일 업로드 (서버 경유)", description = "S3 버킷의 지정된 디렉토리에 여러 파일을 서버를 통해 업로드합니다.")
    @PostMapping("/upload/multiple", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFiles(
        @RequestPart("files") files: List<MultipartFile>,
        @RequestParam(defaultValue = "COMMON") directory: FileDirectory
    ): ResponseEntity<BasicResponse<List<FileUploadResult>>> {
        val results = fileService.uploadFiles(files, directory)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(results, HttpStatus.OK, "다중 파일 업로드에 성공했습니다."))
    }

    @Operation(summary = "단일 파일 삭제", description = "S3 버킷에서 지정된 storageKey에 해당하는 파일을 삭제합니다.")
    @DeleteMapping
    fun deleteFile(
        @RequestParam storageKey: String
    ): ResponseEntity<BasicResponse<Unit>> {
        fileService.deleteFile(storageKey)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(Unit, HttpStatus.OK, "파일 삭제에 성공했습니다."))
    }

    @Operation(summary = "다중 파일 삭제", description = "S3 버킷에서 여러 storageKey에 해당하는 파일들을 일괄 삭제합니다.")
    @DeleteMapping("/multiple")
    fun deleteFiles(
        @RequestBody storageKeys: List<String>
    ): ResponseEntity<BasicResponse<Unit>> {
        fileService.deleteFiles(storageKeys)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(Unit, HttpStatus.OK, "다중 파일 삭제에 성공했습니다."))
    }
}
