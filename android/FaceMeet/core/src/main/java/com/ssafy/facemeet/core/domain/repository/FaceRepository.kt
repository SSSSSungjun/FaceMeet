package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.data.remote.dto.response.FaceAnalysisResponse
import com.ssafy.facemeet.core.data.remote.dto.response.FaceInfoResponse
import okhttp3.MultipartBody

interface FaceRepository {
    suspend fun analyzeFace(
        frontImage: MultipartBody.Part,
        sideImage: MultipartBody.Part
    ): Result<FaceAnalysisResponse>

    suspend fun getMyFaceInfo(): Result<FaceInfoResponse>
}
