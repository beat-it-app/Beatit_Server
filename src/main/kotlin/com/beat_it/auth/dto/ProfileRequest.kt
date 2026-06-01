package com.beat_it.auth.dto

import org.springframework.web.multipart.MultipartFile

data class ProfileRequest(
    val name: String,
    val profileImage: MultipartFile? = null
)
