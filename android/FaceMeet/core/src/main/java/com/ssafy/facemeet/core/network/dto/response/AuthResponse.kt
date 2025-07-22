package com.ssafy.facemeet.core.network.dto.response

data class AuthResponse<T>(
    val status: Int,
    val data: T?,
    val accessToken: String?
) // auth전용
