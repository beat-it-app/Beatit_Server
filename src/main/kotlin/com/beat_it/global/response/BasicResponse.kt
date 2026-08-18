package com.beat_it.global.response

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BasicResponse<T>(
    val success: Boolean,
    val status: Any,
    val message: String?,
    val data: T?
) {
    companion object {
        fun <T> success(data: T, httpStatus: HttpStatus, message: String? = "Request Successful"): BasicResponse<T> =
            BasicResponse(true, httpStatus.value(), message, data)

        fun success(httpStatus: HttpStatus, message: String?): BasicResponse<Nothing> =
            BasicResponse(true, httpStatus.value(), message, null)

        fun fail(code: String, message: String): BasicResponse<Nothing> =
            BasicResponse(false, code, message, null)
    }
}