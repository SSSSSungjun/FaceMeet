package com.ssafy.facemeet.core.domain.model

data class Auth(
    val status: Int,
    val data: () -> Unit,
    val accessToken: String?,
    val refreshToken: String
)