package com.ssafy.facemeet.core.domain.model

data class UserInfo(
    val name: String,
    val email: String,
    val nickname: String = "",
    val gender: String,
    val address: String = "",
    val birth: String,
    val preferAgeLower: Int,
    val preferAgeUpper: Int,
    val latitude: Double,   // 필수 값
    val longitude: Double   // 필수 값
) {
    fun getGenderLabel(): String = when (gender) {
        "m" -> "남자"
        "f" -> "여자"
        else -> "기타"
    }
}