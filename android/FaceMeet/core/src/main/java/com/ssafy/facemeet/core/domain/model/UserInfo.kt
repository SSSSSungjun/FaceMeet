package com.ssafy.facemeet.core.domain.model

data class UserInfo(
    val name: String,
    val email: String,
    val nickname: String,
    val gender: String,
    val address: String,
    val birth: String,
    val preferAgeLower: Int,
    val preferAgeUpper: Int
)