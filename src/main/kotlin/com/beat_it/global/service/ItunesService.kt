package com.beat_it.global.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.beat_it.global.response.ItunesSearchResponse
import com.beat_it.global.response.ItunesTrack
import com.beat_it.global.response.MusicInfo
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ItunesService(
    private val objectMapper: ObjectMapper
) {
    private val restClient = RestClient.create()

    fun searchTracks(query: String, page: Int = 0, limit: Int = 10): List<MusicInfo> {
        val jsonResponse = restClient.get()
            .uri("https://itunes.apple.com/search?term={query}&media=music&entity=song&limit=100", query)
            .retrieve()
            .body(String::class.java) ?: return emptyList()

        val response = objectMapper.readValue(jsonResponse, ItunesSearchResponse::class.java)
        val allResults = response?.results ?: return emptyList()

        val fromIndex = page * limit
        if (fromIndex >= allResults.size) {
            return emptyList()
        }
        val toIndex = minOf(fromIndex + limit, allResults.size)
        val pagedTracks = allResults.subList(fromIndex, toIndex)

        val koreanMetadataMap = fetchKoreanMetadata(pagedTracks.mapNotNull { it.trackId })

        return pagedTracks.map { track ->
            val krTrack = track.trackId?.let { koreanMetadataMap[it] }
            MusicInfo(
                title = krTrack?.trackName ?: track.trackName ?: "Unknown Title",
                artist = krTrack?.artistName ?: track.artistName ?: "Unknown Artist",
                previewUrl = krTrack?.previewUrl ?: track.previewUrl,
                imageUrl = krTrack?.artworkUrl100 ?: track.artworkUrl100,
                duration = (krTrack?.trackTimeMillis ?: track.trackTimeMillis)?.let { formatDuration(it) }
            )
        }
    }

    private fun fetchKoreanMetadata(trackIds: List<Long>): Map<Long, ItunesTrack> {
        if (trackIds.isEmpty()) return emptyMap()
        return try {
            val idParam = trackIds.joinToString(",")
            val lookupJson = restClient.get()
                .uri("https://itunes.apple.com/lookup?id={idParam}&country=KR", idParam)
                .retrieve()
                .body(String::class.java) ?: return emptyMap()

            val lookupResponse = objectMapper.readValue(lookupJson, ItunesSearchResponse::class.java)
            lookupResponse?.results?.filter { it.trackId != null }?.associateBy { it.trackId!! } ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun formatDuration(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}