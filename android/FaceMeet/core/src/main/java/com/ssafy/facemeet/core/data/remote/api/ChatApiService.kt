package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.data.remote.dto.response.ChatListItemResponse
import com.ssafy.facemeet.core.data.remote.dto.response.ChattingAllResponse
import com.ssafy.facemeet.core.data.remote.dto.response.MatchingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {

    @GET("/api/v1/chatrooms")
    suspend fun getChattingList(): Response<MutableList<ChatListItemResponse>>

    @POST("/api/v1/chatrooms")
    suspend fun postChattingList(@Body request: MatchingUserRequest): Response<MatchingResponse>

    @POST("/api/v1/chatrooms/{roomId}")
    suspend fun postChattingLike(
        @Path("roomId") roomId: Long,
        @Query("selected") selected: Boolean
    ): Response<Unit>

    @POST("/api/v1/chatrooms/{roomId}/leave")
    suspend fun postChattingLeave(@Path("roomId") roomId: Long): Response<Unit>

    @GET("/api/v1/chatrooms/{roomId}/messages/all")
    suspend fun getChattingMessagesAll(
        @Path("roomId") roomId: Long,
    ): Response<ChattingAllResponse>

    @GET("/api/v1/chatrooms/{roomId}/messages/last")
    suspend fun getChattingMessagesLast(
        @Path("roomId") roomId: Long,
        @Query("limit") limit: Int = 20
    ): Response<ChattingAllResponse>

}