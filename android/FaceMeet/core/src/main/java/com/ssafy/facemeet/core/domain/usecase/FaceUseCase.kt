package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.data.remote.dto.response.FaceAnalysisResponse
import com.ssafy.facemeet.core.data.remote.dto.response.FaceInfoResponse
import com.ssafy.facemeet.core.domain.repository.FaceRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class GetMyFaceInfoUseCase @Inject constructor(
    private val faceRepository: FaceRepository
) {
    suspend operator fun invoke(): Result<FaceInfoResponse> =
        faceRepository.getMyFaceInfo()
}

class AnalyzeFaceUseCase @Inject constructor(
    private val faceRepository: FaceRepository
) {
    suspend operator fun invoke(
        frontImage: MultipartBody.Part,
        sideImage: MultipartBody.Part
    ): Result<FaceAnalysisResponse> =
        faceRepository.analyzeFace(frontImage, sideImage)
}
