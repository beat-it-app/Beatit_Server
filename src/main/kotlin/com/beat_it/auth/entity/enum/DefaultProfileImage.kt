package com.beat_it.auth.entity.enum

import kotlin.random.Random

enum class DefaultProfileImage(
    val fileName: String,
    val storageKey: String,
    val url: String
) {
    PROFILE_ORANGE(
        "profile_orange.png",
        "profile/default-profiles/profile_orange.png",
        "https://beatit-dev-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile/default-profiles/profile_orange.png"
    ),
    PROFILE_GREEN(
        "profile_green.png",
        "profile/default-profiles/profile_green.png",
        "https://beatit-dev-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile/default-profiles/profile_green.png"
    ),
    PROFILE_BLUE(
        "profile_blue.png",
        "profile/default-profiles/profile_blue.png",
        "https://beatit-dev-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile/default-profiles/profile_blue.png"
    ),
    PROFILE_PINK(
        "profile_pink.png",
        "profile/default-profiles/profile_pink.png",
        "https://beatit-dev-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile/default-profiles/profile_pink.png"
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