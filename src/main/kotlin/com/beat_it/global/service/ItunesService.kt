package com.beat_it.global.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.beat_it.global.response.ItunesSearchResponse
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
            .uri("https://itunes.apple.com/search?term={query}&media=music&entity=song&country=KR&limit=100", query)
            .retrieve()
            .body(String::class.java) ?: return emptyList()

        val response = objectMapper.readValue(jsonResponse, ItunesSearchResponse::class.java)

        val allTracks = response?.results?.map { track ->
            MusicInfo(
                title = track.trackName ?: "Unknown Title",
                artist = track.artistName ?: "Unknown Artist",
                previewUrl = track.previewUrl,
                imageUrl = track.artworkUrl100,
                duration = track.trackTimeMillis?.let { formatDuration(it) }
            )
        } ?: emptyList()

        val fromIndex = page * limit
        if (fromIndex >= allTracks.size) {
            return emptyList()
        }
        val toIndex = minOf(fromIndex + limit, allTracks.size)
        return allTracks.subList(fromIndex, toIndex)
    }

    private fun formatDuration(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}