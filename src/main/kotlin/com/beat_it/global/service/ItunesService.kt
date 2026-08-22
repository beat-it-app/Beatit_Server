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

    fun searchTrack(query: String): MusicInfo? {
        val jsonResponse = restClient.get()
            .uri("https://itunes.apple.com/search?term={query}&media=music&country=KR&limit=1", query)
            .retrieve()
            .body(String::class.java) ?: return null

        val response = objectMapper.readValue(jsonResponse, ItunesSearchResponse::class.java)

        val track = response?.results?.firstOrNull() ?: return null

        return MusicInfo(
            title = track.trackName ?: "Unknown Title",
            artist = track.artistName ?: "Unknown Artist",
            previewUrl = track.previewUrl,
            imageUrl = track.artworkUrl100
        )
    }
}