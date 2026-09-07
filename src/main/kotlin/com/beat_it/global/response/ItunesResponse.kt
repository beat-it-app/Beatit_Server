package com.beat_it.global.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<ItunesTrack>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ItunesTrack(
    val trackName: String?,
    val artistName: String?,
    val previewUrl: String?,
    val artworkUrl100: String?,
    val trackTimeMillis: Long?
)

data class MusicInfo(
    val title: String,
    val artist: String,
    val previewUrl: String?,
    val imageUrl: String?,
    val duration: String?
)