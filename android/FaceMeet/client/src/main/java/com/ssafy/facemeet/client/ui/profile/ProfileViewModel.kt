package com.ssafy.facemeet.client.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.dto.response.FaceInfoResponse
import com.ssafy.facemeet.core.domain.usecase.GetMyFaceInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceInfoViewModel @Inject constructor(
    private val getMyFaceInfoUseCase: GetMyFaceInfoUseCase
) : ViewModel() {

    private val _faceInfo = MutableStateFlow<FaceInfoResponse?>(null)
    val faceInfo: StateFlow<FaceInfoResponse?> = _faceInfo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadFaceInfo() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = getMyFaceInfoUseCase()
                result.onSuccess {
                    _faceInfo.value = it
                    _error.value = null
                }.onFailure {
                    _error.value = it.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
