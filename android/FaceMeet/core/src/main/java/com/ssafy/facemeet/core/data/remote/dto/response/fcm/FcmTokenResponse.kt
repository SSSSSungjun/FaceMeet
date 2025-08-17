package com.ssafy.facemeet.core.data.remote.dto.response.fcm

data class FcmTokenResponse(
    val tokenId: Long,
    val deviceToken: String,
    val deviceType: String,
    val isNew: Boolean,
    val lastUsedAt: String
)
