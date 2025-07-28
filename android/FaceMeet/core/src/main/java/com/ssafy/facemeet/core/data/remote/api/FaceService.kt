package com.ssafy.facemeet.core.data.remote.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FaceService {
    @Multipart
    @POST("api/analyze/")
    suspend fun postAnalyze(
        @Part front_image: MultipartBody.Part,
        @Part side_image: MultipartBody.Part,
    ): Response<ResponseBody>
}
