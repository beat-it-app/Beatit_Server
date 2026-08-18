package com.beat_it.auth.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Collections

@Service
class GoogleAuthService(
    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    private val googleClientId: String
) {
    private val verifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance()
    )
        .setAudience(Collections.singletonList(googleClientId))
        .build()

    fun verifyToken(idTokenString: String): GoogleUserPayload {
        try {
            val idToken = verifier.verify(idTokenString)
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
            val payload = idToken.payload
            
            val email = payload.email ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
            val googleId = payload.subject ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            return GoogleUserPayload(
                googleId = googleId,
                email = email
            )
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
    }
}

data class GoogleUserPayload(
    val googleId: String,
    val email: String
)