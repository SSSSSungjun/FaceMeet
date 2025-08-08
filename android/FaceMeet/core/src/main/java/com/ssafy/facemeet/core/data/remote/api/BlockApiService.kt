package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.response.BlockResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BlockApiService {
    @POST("/api/v1/blocks/{userId}")
    suspend fun postBlockUser(@Path("userId") userId: Long): Response<Unit>

    @GET("/api/v1/blocks")
    suspend fun getBlockList(): Response<MutableList<BlockResponse>>

    @DELETE("/api/v1/blocks/{blockedId}")
    suspend fun deleteBlockUser(@Path("blockedId") blockedId: Long): Response<Unit>
}