package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.data.remote.dto.response.AuthResponse
import com.ssafy.facemeet.core.data.remote.dto.response.ChatItemResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChatApiService {

    @GET("/api/v1/chatrooms")
    suspend fun getChattingList(@Body request: OnboardingRequest): Response<List<ChatItemResponse>>

    @POST("/api/v1/chatrooms")
    suspend fun postChattingList(@Body request: OnboardingRequest): Response<Unit>

    @POST("/api/v1/chatrooms/{roomId}")
    suspend fun postChattingLike(@Body request: OnboardingRequest): Response<AuthResponse<Unit>>

    @POST("/api/v1/chatrooms/{roomId}/leave")
    suspend fun postChattingLeave(@Body request: OnboardingRequest): Response<AuthResponse<Unit>>

    @GET("/api/v1/chatrooms/{roomId}/messages")
    suspend fun getChattingMessages(@Body request: OnboardingRequest): Response<AuthResponse<Unit>>

}