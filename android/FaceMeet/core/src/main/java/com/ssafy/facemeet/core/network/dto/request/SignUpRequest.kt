package com.ssafy.facemeet.core.network.dto.request

data class SignUpRequest(
    val nickname : String?,
    val address : String?,
    val latitude : Float,
    val longitude : Float,
    val preferAgeLower : Int,
    val preferAgeUpper : Int
)
