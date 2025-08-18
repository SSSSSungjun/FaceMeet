package com.ssafy.facemeet.core.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class TopicSubscriptionRequest(
    @SerializedName("topicId")
    val topicId: Int,
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("subscribedDeviceCount")
    val subscribedDeviceCount: Int,
    @SerializedName("subscribedTokens")
    val subscribedTokens: List<String>,
    @SerializedName("failedTokens")
    val failedTokens: List<String>
)