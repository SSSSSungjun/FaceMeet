package com.ssafy.facemeet.core.data.remote.dto.request

data class OnboardingRequest(
    val nickname: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val preferAgeLower: Int,
    val preferAgeUpper: Int,
)
