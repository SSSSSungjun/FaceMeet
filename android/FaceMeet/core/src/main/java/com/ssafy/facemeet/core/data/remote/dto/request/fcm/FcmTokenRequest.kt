package com.ssafy.facemeet.core.data.remote.dto.request.fcm

data class FcmTokenRequest(
    val deviceToken: String,
    val deviceType: String = "android", // 고정
    val deviceId: String
)
