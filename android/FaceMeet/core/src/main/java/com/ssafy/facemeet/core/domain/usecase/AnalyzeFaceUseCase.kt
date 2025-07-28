package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.data.remote.dto.response.FaceAnalysisResponse
import com.ssafy.facemeet.core.domain.repository.FaceRepository
import okhttp3.MultipartBody

class AnalyzeFaceUseCase(
    private val repository: FaceRepository
) {
    suspend operator fun invoke(
        frontImage: MultipartBody.Part,
        sideImage: MultipartBody.Part
    ): Result<FaceAnalysisResponse> {
        return repository.analyzeFace(frontImage, sideImage)
    }
}
