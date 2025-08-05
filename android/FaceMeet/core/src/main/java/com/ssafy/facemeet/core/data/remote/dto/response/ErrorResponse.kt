package com.ssafy.facemeet.core.data.remote.dto.response

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String
)
