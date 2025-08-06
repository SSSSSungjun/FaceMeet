package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.UserInfoModRequest
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    @POST("/api/v1/users/online")
    suspend fun postOnline(): Response<Unit>

    @POST("/api/v1/users/offline")
    suspend fun postOffline(): Response<Unit>

    @GET("/api/v1/users/me")
    suspend fun getUserInfo(): Response<UserInfoResponse>

    @DELETE("/api/v1/users/me")
    suspend fun deleteUser(): Response<Unit>

    @PATCH("/api/v1/users/me")
    suspend fun patchUserInfo(@Body request: UserInfoModRequest): Response<UserInfoResponse>

    @GET("/api/v1/users/partner/{partnerId}")
    suspend fun getPartnerFaceInfo(@Path("partnerId") partnerId: Long): Response<PartnerFaceInfoResponse>

//    @GET("/api/v1/users/status")
//    suspend fun getUserStatus(): Response<AuthResponse<Unit>>
}