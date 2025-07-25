package com.ssafy.facemeet.core.domain.model

data class Auth(
    val status: Int,
    val accessToken: String?,
    val data: () -> Unit
)