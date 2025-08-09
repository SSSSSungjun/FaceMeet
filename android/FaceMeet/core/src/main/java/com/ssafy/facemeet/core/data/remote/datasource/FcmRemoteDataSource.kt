package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.FcmApiService
import com.ssafy.facemeet.core.data.remote.dto.request.TopicSubscriptionRequest
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.FcmTokenResponse
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.TopicSubscriptionResponse
import retrofit2.Response
import javax.inject.Inject

class FcmRemoteDataSource @Inject constructor(
    private val apiService: FcmApiService
) {
    suspend fun getDeviceTokens(): Response<List<FcmTokenResponse>> {
        return apiService.getDeviceTokens()
    }

    suspend fun registerDevice(request: FcmTokenRequest): Response<FcmTokenResponse> {
        return apiService.registerDevice(request)
    }

    suspend fun deleteDevice(deviceToken: String): Response<Unit> {
        return apiService.deleteDevice(deviceToken)
    }

    suspend fun postSubscription(topicId: Long): Response<TopicSubscriptionRequest> {
        return apiService.postSubscription(topicId)
    }

    suspend fun deleteSubscription(topicId: Long): Response<Unit> {
        return apiService.deleteSubscription(topicId)
    }

    suspend fun getSubscriptions(): Response<TopicSubscriptionResponse> {
        return apiService.getSubscriptions()
    }
}