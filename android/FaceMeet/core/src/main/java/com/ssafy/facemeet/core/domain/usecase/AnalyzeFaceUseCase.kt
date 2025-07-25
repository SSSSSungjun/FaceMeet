package com.ssafy.facemeet.core.domain.usecase

import com.google.gson.JsonObject
import com.ssafy.facemeet.core.domain.repository.FaceRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AnalyzeFaceUseCase(
    private val repository: FaceRepository
) {
    suspend operator fun invoke(
        image1: MultipartBody.Part,
        sideImage1: MultipartBody.Part,
        userId: RequestBody?
    ): Result<JsonObject> {
        return repository.analyzeFace(image1, sideImage1, userId)
    }
}
