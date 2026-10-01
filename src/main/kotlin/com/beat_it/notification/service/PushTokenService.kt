package com.beat_it.notification.service

import com.beat_it.notification.dto.PushTokenRegisterRequest
import com.beat_it.notification.dto.PushTokenResponse
import com.beat_it.notification.entity.PushTokens
import com.beat_it.notification.repository.PushTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushTokenService(
    private val pushTokenRepository: PushTokenRepository
) {

    /**
     * 디바이스의 푸시 토큰 등록 또는 갱신
     */
    @Transactional
    fun registerOrUpdateToken(userId: Long, request: PushTokenRegisterRequest): PushTokenResponse {
        val existingToken = pushTokenRepository.findByDeviceIdAndUserId(request.deviceId, userId)

        val savedToken = if (existingToken != null) {
            existingToken.updateToken(
                token = request.pushToken,
                platform = request.platformType,
                version = request.appVersion
            )
            existingToken
        } else {
            val newToken = PushTokens(
                userId = userId,
                deviceId = request.deviceId,
                platformType = request.platformType,
                pushToken = request.pushToken,
                appVersion = request.appVersion,
                isActive = true
            )
            pushTokenRepository.save(newToken)
        }

        return PushTokenResponse(
            pushTokenId = savedToken.pushTokenId ?: 0L,
            deviceId = savedToken.deviceId,
            platformType = savedToken.platformType,
            isActive = savedToken.isActive
        )
    }

    /**
     * 로그아웃 또는 푸시 비활성화 시 토큰 비활성화 처리
     */
    @Transactional
    fun deactivateToken(userId: Long, deviceId: String) {
        val token = pushTokenRepository.findByDeviceIdAndUserId(deviceId, userId)
        token?.deactivate()
    }

    /**
     * 회원 탈퇴 시 해당 회원의 모든 푸시 토큰 비활성화
     */
    @Transactional
    fun deactivateAllUserTokens(userId: Long) {
        val tokens = pushTokenRepository.findAllByUserIdAndIsActiveTrue(userId)
        tokens.forEach { it.deactivate() }
    }
}
