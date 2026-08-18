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
            latitude = request.latitude,
            longitude = request.longitude,
            mapUrl = request.mapUrl,
            phone = request.phone,
            kakaoPlaceId = request.kakaoPlaceId,
            jibunAddress = request.jibunAddress
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
    fun searchLocations(
        query: String,
        latitude: BigDecimal? = null,
        longitude: BigDecimal? = null
    ): List<LocationSearchResponse> {
        if (kakaoRestApiKey.isBlank()) {
            throw BusinessException(ErrorCode.INVALID_REQUEST_BODY)
        }

        val response = restClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .apply {
                        if (longitude != null) queryParam("x", longitude.toPlainString())
                        if (latitude != null) queryParam("y", latitude.toPlainString())
                        if (longitude != null && latitude != null) queryParam("sort", "distance")
                    }
                    .build()
            }
            .retrieve()
            .body(KakaoSearchResponse::class.java)

        val searchResults = response?.documents?.map { doc ->
            LocationSearchResponse(
                locationName = doc.placeName,
                roadAddress = doc.roadAddressName.ifBlank { doc.addressName },
                latitude = BigDecimal(doc.y),
                longitude = BigDecimal(doc.x),
                mapUrl = doc.placeUrl,
                phone = doc.phone,
                kakaoPlaceId = doc.id,
                jibunAddress = doc.addressName,
                distance = doc.distance
            )
        } ?: emptyList()

        return if (longitude != null && latitude != null) {
            searchResults.sortedBy { it.distance?.toIntOrNull() ?: Int.MAX_VALUE }
        } else {
            searchResults
        }
    }
}
