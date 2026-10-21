package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.Role
import com.beat_it.auth.entity.enum.SocialProvider
import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(
    val userId: Long?,
    val role: Role,
    @get:JsonProperty("isCreatedProfile")
    val isCreatedProfile: Boolean,
    val socialProvider: SocialProvider? = null
)


