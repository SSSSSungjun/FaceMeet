package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.TopicSubscriptionRequest
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.FcmTokenResponse
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.TopicSubscriptionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FcmApiService {

    @GET("/api/v1/fcm/tokens")
    suspend fun getDeviceTokens(): Response<List<FcmTokenResponse>>

    @PUT("/api/v1/fcm/tokens")
    suspend fun registerDevice(
        @Body request: FcmTokenRequest
    ): Response<FcmTokenResponse>

    @DELETE("/api/v1/fcm/tokens")
    suspend fun deleteDevice(deviceToken: String) : Response<Unit>

    @POST("/api/v1/fcm/topics/{topicId}/subscriptions")
    suspend fun postSubscription(@Path("topicId") topicId: String) : Response<TopicSubscriptionRequest>

    @DELETE("/api/v1/fcm/topics/{topicId}/subscriptions")
    suspend fun deleteSubscription(@Path("topicId") topicId: String) : Response<Unit>

    @GET("/api/v1/fcm/topics/subscriptions")
    suspend fun getSubscriptions() : Response<TopicSubscriptionResponse>

}