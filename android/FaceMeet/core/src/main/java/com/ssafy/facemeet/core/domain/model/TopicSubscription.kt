package com.ssafy.facemeet.core.domain.model

import com.google.gson.annotations.SerializedName

data class TopicSubscription(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("topicName")
    val topicName: List<String>
)