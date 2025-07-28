package com.ssafy.facemeet.core.domain.repository

import okhttp3.MultipartBody
import okhttp3.ResponseBody

interface FaceRepository {
    suspend fun analyzeFace(
        frontImage: MultipartBody.Part,
        sideImage: MultipartBody.Part
    ): Result<ResponseBody>
}
