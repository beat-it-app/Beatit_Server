package com.beat_it.global.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtil {
    private val DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun format(dateTime: OffsetDateTime): String {
        return dateTime.format(DEFAULT_FORMATTER)
    }
}
