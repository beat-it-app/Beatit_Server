package com.beat_it.auth.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class NaverAuthService {

    private val restClient: RestClient = RestClient.builder()
        .baseUrl("https://openapi.naver.com")
        .build()

    fun verifyToken(accessToken: String): NaverUserPayload {
        try {
            val response = restClient.get()
                .uri("/v1/nid/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .onStatus(HttpStatusCode::isError) { _, _ ->
                    throw BusinessException(ErrorCode.INVALID_TOKEN)
                }
                .body(NaverUserInfoResponse::class.java)
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            val naverAccount = response.response
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            val naverId = naverAccount.id
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
            val email = naverAccount.email
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            return NaverUserPayload(
                naverId = naverId,
                email = email
            )
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class NaverUserInfoResponse(
    val resultcode: String? = null,
    val message: String? = null,
    val response: NaverAccount? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NaverAccount(
    val id: String? = null,
    val email: String? = null
)

data class NaverUserPayload(
    val naverId: String,
    val email: String
)