package com.ssafy.facemeet.core.data.remote.mapper

import com.ssafy.facemeet.core.data.remote.dto.response.fcm.FcmTokenResponse
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.TopicSubscriptionResponse
import com.ssafy.facemeet.core.domain.model.FcmToken
import com.ssafy.facemeet.core.domain.model.TopicSubscription

fun FcmTokenResponse.toDomain(): FcmToken {
    return FcmToken(
        tokenId = this.tokenId,
        deviceToken = this.deviceToken,
        deviceType = this.deviceType,
        isNew = this.isNew,
        lastUsedAt = this.lastUsedAt
    )
}

fun TopicSubscriptionResponse.toDomain(): TopicSubscription {
    return TopicSubscription(
        userId = this.userId,
        topicName = this.topicName
    )
}
