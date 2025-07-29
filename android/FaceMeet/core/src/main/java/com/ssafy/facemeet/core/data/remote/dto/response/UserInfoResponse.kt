package com.ssafy.facemeet.core.data.remote.dto.response

import java.time.LocalDateTime

data class UserInfoResponse(
    val name: String,
    val email: String,
    val nickname: String,
    val gender: String,
    val address: String,
    val birth: LocalDateTime,
    val preferAgeLower: Int,
    val preferAgeUpper: Int
)
