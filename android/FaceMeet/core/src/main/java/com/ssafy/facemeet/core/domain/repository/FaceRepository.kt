package com.ssafy.facemeet.core.domain.repository

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface FaceRepository {
    suspend fun analyzeFace(
        image1: MultipartBody.Part,
        sideImage1: MultipartBody.Part,
        userId: RequestBody?
    ): Result<JsonObject>
}
