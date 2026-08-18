package com.beat_it.location.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.location.dto.KakaoSearchResponse
import com.beat_it.location.dto.LocationRequest
import com.beat_it.location.dto.LocationResponse
import com.beat_it.location.dto.LocationSearchResponse
import com.beat_it.location.entity.Locations
import com.beat_it.location.repository.LocationsRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Service
class LocationsService(
    private val locationsRepository: LocationsRepository,
    @Value("\${kakao.rest-api-key}") private val kakaoRestApiKey: String
) {
    private val restClient: RestClient by lazy {
        RestClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Authorization", "KakaoAK $kakaoRestApiKey")
            .build()
    }

    @Transactional
    fun createLocation(userId: Long, request: LocationRequest): LocationResponse {
        val location = Locations(
            userId = userId,
            locationName = request.locationName,
            roadAddress = request.roadAddress,
            detailAddress = request.detailAddress,
            latitude = request.latitude,
            longitude = request.longitude,
            mapUrl = request.mapUrl
        )
        val saved = locationsRepository.save(location)
        return LocationResponse.from(saved)
    }

    @Transactional(readOnly = true)
    fun getLocation(locationId: Long): LocationResponse {
        val location = locationsRepository.findById(locationId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }
        return LocationResponse.from(location)
    }

    @Transactional(readOnly = true)
    fun searchLocations(query: String): List<LocationSearchResponse> {
        if (kakaoRestApiKey.isBlank()) {
            throw BusinessException(ErrorCode.INVALID_REQUEST_BODY)
        }

        val response = restClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .build()
            }
            .retrieve()
            .body(KakaoSearchResponse::class.java)

        return response?.documents?.map { doc ->
            LocationSearchResponse(
                locationName = doc.placeName,
                roadAddress = doc.roadAddressName.ifBlank { doc.addressName },
                detailAddress = if (doc.roadAddressName.isNotBlank() && doc.addressName != doc.roadAddressName) doc.addressName else null,
                latitude = BigDecimal(doc.y),
                longitude = BigDecimal(doc.x),
                mapUrl = doc.placeUrl
            )
        } ?: emptyList()
    }
}
