package com.ssafy.facemeet.core.domain.model

data class FcmToken(
    val tokenId: Long,
    val deviceToken: String,
    val deviceType: String,
    val isNew: Boolean,
    val lastUsedAt: String
)
