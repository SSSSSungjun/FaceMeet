package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.domain.repository.FaceRepository
import okhttp3.MultipartBody
import okhttp3.ResponseBody

class AnalyzeFaceUseCase(
    private val repository: FaceRepository
) {
    suspend operator fun invoke(
        frontImage: MultipartBody.Part,
        sideImage: MultipartBody.Part
    ): Result<ResponseBody> {
        return repository.analyzeFace(frontImage, sideImage)
    }
}
