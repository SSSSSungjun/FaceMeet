package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.FcmTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

interface FcmService {
    @PUT("api/v1/fcm/tokens")
    fun registerDevice(
        @Body request: FcmTokenRequest
    ): Call<FcmTokenResponse>
}