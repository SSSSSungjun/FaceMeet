package com.ssafy.facemeet.core.data.remote.dto.response.fcm

import com.google.gson.annotations.SerializedName

data class TopicSubscriptionResponse(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("topicName")
    val topicName: List<String>
)