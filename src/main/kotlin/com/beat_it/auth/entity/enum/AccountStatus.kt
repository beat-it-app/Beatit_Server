package com.beat_it.auth.entity.enum

enum class AccountStatus(val description: String) {
    ACTIVE("활성"),
    WITHDRAWN("탈퇴"),
    BANNED("정지");
}