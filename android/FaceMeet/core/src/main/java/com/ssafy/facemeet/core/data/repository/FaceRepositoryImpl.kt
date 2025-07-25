package com.ssafy.facemeet.core.data.repository

import android.util.Log
import com.google.gson.JsonObject
import com.ssafy.facemeet.core.data.remote.api.FaceService
import com.ssafy.facemeet.core.domain.repository.FaceRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class FaceRepositoryImpl @Inject constructor(
    private val faceService: FaceService
) : FaceRepository {
    override suspend fun analyzeFace(
        image1: MultipartBody.Part,
        sideImage1: MultipartBody.Part,
        userId: RequestBody?
    ): Result<JsonObject> {
        return try {
            Log.d("FlowCheck", "🟡 analyzeFace() 호출됨")

            val response = faceService.postAnalyze(image1, sideImage1, userId)

            Log.d("FlowCheck", "🟢 응답 수신. code=${response.code()}, success=${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                Log.d("FlowCheck", "✅ 응답 성공! body=${response.body()}")
                Result.success(response.body()!!)
            } else {
                Log.e(
                    "FlowCheck",
                    "❌ 응답 실패. code=${response.code()}, message=${response.message()}"
                )
                Result.failure(Exception("분석 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("FlowCheck", "🔥 예외 발생: ${e.message}", e)
            Result.failure(e)
        }
    }
}
