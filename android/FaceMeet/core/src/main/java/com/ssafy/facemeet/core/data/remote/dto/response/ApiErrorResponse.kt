package com.ssafy.facemeet.core.data.remote.dto.response

data class ApiError(
    val status: Int,
    val code: String,
    val message: String
)
