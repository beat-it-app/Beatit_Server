package com.beat_it.auth.dto

data class SignUpResponse (
    val userId: Long,
    val identifier: String,
    val email: String,
    val createdAt: String
) 