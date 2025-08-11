package com.ssafy.facemeet.core.data.remote.dto.response

data class UserInfoResponse(
    val name: String,
    val email: String,
    val nickname: String,
    val gender: String,
    val address: String,
    val birth: String,
    val preferAgeLower: Int,
    val preferAgeUpper: Int,
    val latitude: Double,   // 필수
    val longitude: Double   // 필수
)