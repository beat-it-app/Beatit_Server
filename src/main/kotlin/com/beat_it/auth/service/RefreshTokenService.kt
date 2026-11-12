package com.beat_it.auth.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RefreshTokenService(
    private val redisTemplate: StringRedisTemplate
) {
    private val tokenPrefix = "refreshToken:"

    fun saveRefreshToken(userId: String, refreshToken: String, expirationMs: Long) {
        val key = "$tokenPrefix$userId"
        redisTemplate.opsForValue().set(key, refreshToken, expirationMs, TimeUnit.MILLISECONDS)
    }

    fun getRefreshToken(userId: String): String? {
        val key = "$tokenPrefix$userId"
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteRefreshToken(userId: String) {
        val key = "$tokenPrefix$userId"
        redisTemplate.delete(key)
    }
}
