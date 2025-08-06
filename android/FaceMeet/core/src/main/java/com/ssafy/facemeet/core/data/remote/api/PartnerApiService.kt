package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.response.FaceInfoResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PartnerApiService {
    @GET("/api/v1/faces/{partnerId}")
    suspend fun getPartnerFaceInfo(@Path("partnerId") partnerId: Long): FaceInfoResponse
}