package com.beat_it.auth.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

@Service
class AppleAuthService(
    @Value("\${apple.bundle-id}")
    private val appleBundleId: String,
    private val objectMapper: ObjectMapper
) {
    private val restClient: RestClient = RestClient.builder()
        .baseUrl("https://appleid.apple.com")
        .build()

    fun verifyToken(identityToken: String): AppleUserPayload {
        try {
            val tokenParts = identityToken.split(".")
            if (tokenParts.size != 3) {
                throw BusinessException(ErrorCode.INVALID_TOKEN)
            }

            val headerJson = String(Base64.getUrlDecoder().decode(tokenParts[0]))
            val headerNode = objectMapper.readTree(headerJson)
            val kid = headerNode.get("kid")?.asText() ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
            val alg = headerNode.get("alg")?.asText() ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            val response = restClient.get()
                .uri("/auth/keys")
                .retrieve()
                .body(ApplePublicKeysResponse::class.java)
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            val matchedKey = response.keys.firstOrNull { it.kid == kid && it.alg == alg }
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            val publicKey = generatePublicKey(matchedKey.n, matchedKey.e)

            val claims: Claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(identityToken)
                .payload

            val issuer = claims.issuer
            val audience = claims.audience

            if (issuer != "https://appleid.apple.com") {
                throw BusinessException(ErrorCode.INVALID_TOKEN)
            }
            if (!audience.contains(appleBundleId)) {
                throw BusinessException(ErrorCode.INVALID_TOKEN)
            }

            val appleId = claims.subject ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
            val email = claims.get("email", String::class.java)

            return AppleUserPayload(
                appleId = appleId,
                email = email
            )
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
    }

    private fun generatePublicKey(n: String, e: String): PublicKey {
        val nBytes = Base64.getUrlDecoder().decode(n)
        val eBytes = Base64.getUrlDecoder().decode(e)

        val modulus = BigInteger(1, nBytes)
        val publicExponent = BigInteger(1, eBytes)

        val keySpec = RSAPublicKeySpec(modulus, publicExponent)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplePublicKeysResponse(
    val keys: List<ApplePublicKey> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplePublicKey(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String
)

data class AppleUserPayload(
    val appleId: String,
    val email: String?
)
