package com.beat_it.auth.dto

data class LoginRequest(
    val identifier: String,
    val password: String
)
