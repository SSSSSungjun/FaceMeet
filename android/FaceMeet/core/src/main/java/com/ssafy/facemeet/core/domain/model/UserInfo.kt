package com.ssafy.facemeet.core.domain.model

data class UserInfo(
    val name: String,
    val email: String,
    val nickname: String = "",
    val gender: String,
    val address: String = "",
    val birth: String,
    val preferAgeLower: Int,
    val preferAgeUpper: Int
) {
    fun getGenderLabel(): String {
        return when (gender) {
            "m" -> "남자"
            "f" -> "여자"
            else -> "기타"
        }
    }
}