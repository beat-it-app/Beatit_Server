package com.beat_it.team.dto

import java.math.BigDecimal
import java.math.RoundingMode

data class TeamCloudStorageResponse(
    val teamName: String,
    val usagePercentage: Int,
    val totalStorageBytes: Long,
    val totalStorageDisplay: String,
    val usedStorageBytes: Long,
    val usedStorageDisplay: String,
    val remainingStorageBytes: Long,
    val remainingStorageDisplay: String,
    val categories: List<CategoryUsageDetail>
) {
    data class CategoryUsageDetail(
        val category: String,
        val bytes: Long,
        val displaySize: String
    )

    companion object {
        private const val KB = 1024L
        private const val MB = 1024L * 1024L
        private const val GB = 1024L * 1024L * 1024L
        private const val TB = 1024L * 1024L * 1024L * 1024L

        fun formatBytes(bytes: Long): String {
            if (bytes <= 0L) return "0 B"

            val (value, unit) = when {
                bytes >= TB -> (bytes.toDouble() / TB) to "TB"
                bytes >= GB -> (bytes.toDouble() / GB) to "GB"
                bytes >= MB -> (bytes.toDouble() / MB) to "MB"
                bytes >= KB -> (bytes.toDouble() / KB) to "KB"
                else -> bytes.toDouble() to "B"
            }

            val formatted = BigDecimal(value)
                .setScale(2, RoundingMode.FLOOR)
                .stripTrailingZeros()
                .toPlainString()

            return "$formatted $unit"
        }
    }
}