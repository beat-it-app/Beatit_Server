package com.beat_it.auth.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class KakaoAuthService {

    private val restClient: RestClient = RestClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build()

    fun verifyToken(accessToken: String): KakaoUserPayload {
        try {
            val response = restClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::isError) { _, _ ->
                    throw BusinessException(ErrorCode.INVALID_TOKEN)
                }
                .body(KakaoUserInfoResponse::class.java)
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            val kakaoId = response.id.toString()
            val email = response.kakaoAccount?.email
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            return KakaoUserPayload(
                kakaoId = kakaoId,
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
data class KakaoUserInfoResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoAccount(
    val email: String? = null
)

data class KakaoUserPayload(
    val kakaoId: String,
    val email: String
)