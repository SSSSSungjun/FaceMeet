package com.ssafy.facemeet.core.data.remote.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FaceService {
    @Multipart
    @POST("api/analyze/")
    suspend fun postAnalyze(
        @Part image1: MultipartBody.Part,
        @Part sideImage1: MultipartBody.Part,
        @Part("user_id") userId: RequestBody? = null
    ): Response<JsonObject>
}
