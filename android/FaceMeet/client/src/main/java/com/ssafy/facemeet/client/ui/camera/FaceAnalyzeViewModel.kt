package com.ssafy.facemeet.client.ui.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.dto.response.FaceAnalysisResponse
import com.ssafy.facemeet.core.domain.usecase.AnalyzeFaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class FaceAnalyzeViewModel @Inject constructor(
    private val analyzeFaceUseCase: AnalyzeFaceUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _result = MutableStateFlow<FaceAnalysisResponse?>(null)
    val result: StateFlow<FaceAnalysisResponse?> = _result

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun analyzeFace(
        frontImage: MultipartBody.Part,
        sideImage: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                Log.d("FlowCheck", "✅ ViewModel: analyzeFace() 호출됨")
                _isLoading.value = true
                _error.value = null
                _result.value = null

                val result = analyzeFaceUseCase(frontImage, sideImage)

                Log.d("FlowCheck", "✅ UseCase 실행 완료")

                result.onSuccess {
                    Log.d("FlowCheck", "✅ 결과 수신 성공")
                    _result.value = it
                }.onFailure {
                    Log.e("FlowCheck", "❌ 실패", it)
                    _error.value = it.message
                }
            } catch (e: Exception) {
                Log.e("FlowCheck", "❌ 예외 발생", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
