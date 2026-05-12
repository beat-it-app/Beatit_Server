package com.beat_it.global.response

data class BasicResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
) {
    companion object {
        fun <T> success(data: T, message: String? = "Request Successful"): BasicResponse<T> =
            BasicResponse("success", message, data)

        fun fail(message: String?): BasicResponse<Nothing> =
            BasicResponse("fail", message, null)
    }
}