package com.beat_it.location.service

import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.location.dto.KakaoSearchResponse
import com.beat_it.location.dto.LocationRequest
import com.beat_it.location.dto.LocationResponse
import com.beat_it.location.dto.LocationSearchResponse
import com.beat_it.location.entity.Locations
import com.beat_it.location.repository.LocationsRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.math.BigDecimal

@Service
class LocationsService(
    private val locationsRepository: LocationsRepository,
    @Value("\${kakao.rest-api-key:}") private val kakaoRestApiKey: String
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val restClient: RestClient by lazy {
        RestClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Authorization", "KakaoAK $kakaoRestApiKey")
            .build()
    }

    @Transactional
    fun createLocation(userId: Long, request: LocationRequest): Pair<LocationResponse, Boolean> {
        val existing = request.kakaoPlaceId?.takeIf { it.isNotBlank() }?.let { locationsRepository.findByKakaoPlaceId(it) }
        if (existing != null) {
            return Pair(LocationResponse.from(existing), false)
        }

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
        return Pair(LocationResponse.from(saved), true)
    }

    @Transactional(readOnly = true)
    fun getLocation(locationId: Long): LocationResponse {
        val location = locationsRepository.findById(locationId)
            .orElseThrow { BusinessException(ErrorCode.LOCATION_NOT_FOUND) }
        return LocationResponse.from(location)
    }

    @Transactional(readOnly = true)
    fun searchLocations(
        query: String,
        latitude: BigDecimal? = null,
        longitude: BigDecimal? = null
    ): List<LocationSearchResponse> {
        if (query.isBlank()) {
            return emptyList()
        }

        if (kakaoRestApiKey.isBlank()) {
            log.warn("Kakao REST API Key is not configured.")
            return emptyList()
        }

        val response = try {
            restClient.get()
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
                .body<KakaoSearchResponse>()
        } catch (e: Exception) {
            log.error("Failed to search locations from Kakao API: query={}", query, e)
            return emptyList()
        }

        val searchResults = response?.documents?.map { doc ->
            LocationSearchResponse(
                locationName = doc.placeName,
                roadAddress = doc.roadAddressName.ifBlank { doc.addressName },
                latitude = doc.y.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                longitude = doc.x.toBigDecimalOrNull() ?: BigDecimal.ZERO,
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

    @Transactional(readOnly = true)
    fun validateLocationExists(locationId: Long) {
        if (!locationsRepository.existsById(locationId)) {
            throw BusinessException(ErrorCode.LOCATION_NOT_FOUND)
        }
    }

    fun findLocation(locationId: Long): Locations {
        return locationsRepository.findById(locationId)
            .orElseThrow { BusinessException(ErrorCode.LOCATION_NOT_FOUND) }
    }
}
