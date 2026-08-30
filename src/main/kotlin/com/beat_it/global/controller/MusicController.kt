package com.beat_it.global.controller

import com.beat_it.global.response.BasicResponse
import com.beat_it.global.service.ItunesService
import com.beat_it.global.response.MusicInfo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/music")
class MusicController(
    private val itunesService: ItunesService
) {

    @GetMapping("/search")
    fun searchMusic(
        @RequestParam query: String
    ): ResponseEntity<BasicResponse<List<MusicInfo>>> {
        val searchResult = itunesService.searchTrack(query)
        val responseList = if (searchResult != null) listOf(searchResult) else emptyList()

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BasicResponse.success(responseList, HttpStatus.OK, "음악 검색에 성공했습니다."))
    }
}