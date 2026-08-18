package com.beat_it.auth.entity.enum

import kotlin.random.Random

enum class DefaultProfileImage(
    val fileName: String,
    val storageKey: String,
    val url: String
) {
    PROFILE_ORANGE(
        "profile_orange.png",
        "default-profiles/profile_orange.png",
        "https://your-bucket-name.s3.ap-northeast-2.amazonaws.com/default-profiles/profile_orange.png"
    ),
    PROFILE_GREEN(
        "profile_green.png",
        "default-profiles/profile_green.png",
        "https://your-bucket-name.s3.ap-northeast-2.amazonaws.com/default-profiles/profile_green.png"
    ),
    PROFILE_BLUE(
        "profile_blue.png",
        "default-profiles/profile_blue.png",
        "https://your-bucket-name.s3.ap-northeast-2.amazonaws.com/default-profiles/profile_blue.png"
    ),
    PROFILE_PINK(
        "profile_pink.png",
        "default-profiles/profile_pink.png",
        "https://your-bucket-name.s3.ap-northeast-2.amazonaws.com/default-profiles/profile_pink.png"
    );

    companion object {
        fun getRandom(): DefaultProfileImage {
            val entries = entries
            val randomIndex = Random.nextInt(entries.size)
            return entries[randomIndex]
        }

        fun getByIndex(index: Int): DefaultProfileImage {
            val adjustedIndex = index - 1
            val safeIndex = (adjustedIndex % entries.size).let { if (it < 0) it + entries.size else it }
            return entries[safeIndex]
        }
    }
}