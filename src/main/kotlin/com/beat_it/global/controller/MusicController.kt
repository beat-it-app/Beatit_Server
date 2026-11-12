package com.beat_it.global.controller

import com.beat_it.global.response.BasicResponse
import com.beat_it.global.service.ItunesService
import com.beat_it.global.response.MusicInfo
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "8-1. (GLOBAL) MUSIC API", description = "음악 검색")
@RestController
@RequestMapping("/musics")
class MusicController(
    private val itunesService: ItunesService
) {

    @GetMapping("/search")
    fun searchMusic(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<BasicResponse<List<MusicInfo>>> {
        val searchResults = itunesService.searchTracks(query, page, limit)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(searchResults, HttpStatus.OK, "음악 검색에 성공했습니다."))
    }
}