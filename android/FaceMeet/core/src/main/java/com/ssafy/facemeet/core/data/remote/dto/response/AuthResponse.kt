package com.ssafy.facemeet.core.data.remote.dto.response

data class AuthResponse<T>(
    val status: Int,
    val data: T?,
    val accessToken: String?,
    val refreshToken : String? = "null",
) // auth전용
