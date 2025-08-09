package com.ssafy.facemeet.core.domain.repository// FcmRepository.kt
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.domain.model.FcmToken
import com.ssafy.facemeet.core.domain.model.TopicSubscription

interface FcmRepository {
    suspend fun getDeviceTokens(): Result<List<FcmToken>>
    suspend fun registerDevice(request: FcmTokenRequest): Result<FcmToken>
    suspend fun deleteDevice(deviceToken: String): Result<Unit>
    suspend fun postSubscription(topicId: Long): Result<Unit>
    suspend fun deleteSubscription(topicId: Long): Result<Unit>
    suspend fun getSubscriptions(): Result<TopicSubscription>
}