package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.response.FaceAnalysisResponse
import com.ssafy.facemeet.core.data.remote.dto.response.FaceInfoResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FaceService {
    @Multipart
    @POST("/django/api/v1/analyze/")
    suspend fun postAnalyze(
        @Part front_image: MultipartBody.Part,
        @Part side_image: MultipartBody.Part,
    ): Response<FaceAnalysisResponse>

    @GET("/api/v1/users/me/face")
    suspend fun getMyFaceInfo(): FaceInfoResponse


}


