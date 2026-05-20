package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.SocialProvider
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import lombok.Getter

@Getter
data class SignUpRequest (
    // 일반 회원가입 시 필수, 소셜 회원가입 시 null
    val identifier: String?,

    // 일반 회원가입 시 필수, 소셜 회원가입 시 null
    val password: String?,

    val email: String,

    // 소셜 회원가입 시 필수, 일반 회원가입 시 null
    @Enumerated(EnumType.STRING)
    val provider: SocialProvider?,

    // 소셜 회원가입 시 필수, 일반 회원가입 시 null
    @JsonProperty("social_id")
    val socialId: String?,
)