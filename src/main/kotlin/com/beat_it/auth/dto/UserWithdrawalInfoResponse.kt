package com.beat_it.auth.dto

data class UserWithdrawalInfoResponse(
    val teamCount: Long,
    val fileCount: Long,
    val sharedMusicCount: Long,
    val scheduleCount: Long
)
