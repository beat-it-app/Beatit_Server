package com.beat_it.performance.dto

import com.beat_it.performance.entity.PerformanceFiles
import com.beat_it.performance.entity.enum.PerformanceFileType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공연 파일 정보 응답 DTO")
data class PerformanceFileResponse(
    @Schema(description = "공연 파일 ID", example = "1")
    val fileId: Long,

    @Schema(description = "원본 파일명", example = "poster.png")
    val originalFileName: String,

    @Schema(description = "CDN URL", example = "https://cdn.example.com/poster.png")
    val cdnUrl: String,

    @Schema(description = "미리보기 CDN URL", example = "https://cdn.example.com/preview_poster.png")
    val previewCdnUrl: String?,

    @Schema(description = "파일 역할 (POSTER: 포스터, DETAIL_IMAGE: 공연 상세 이미지)", example = "POSTER")
    val fileType: PerformanceFileType,

    @Schema(description = "노출 순서", example = "0")
    val displayOrder: Int
) {
    companion object {
        fun from(file: PerformanceFiles): PerformanceFileResponse {
            return PerformanceFileResponse(
                fileId = file.performanceFileId!!,
                originalFileName = file.originalFileName,
                cdnUrl = file.cdnUrl,
                previewCdnUrl = file.previewCdnUrl,
                fileType = file.fileType,
                displayOrder = file.displayOrder
            )
        }
    }
}
