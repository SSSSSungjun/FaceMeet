package com.ssafy.facemeet.client.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.dto.response.FaceInfoResponse
import com.ssafy.facemeet.core.domain.usecase.GetMatchRemainUseCase
import com.ssafy.facemeet.core.domain.usecase.GetMyFaceInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyFaceInfoUseCase: GetMyFaceInfoUseCase,
    private val getMatchRemainUseCase: GetMatchRemainUseCase,
) : ViewModel() {

    private val _faceInfo = MutableStateFlow<FaceInfoResponse?>(null)
    val faceInfo: StateFlow<FaceInfoResponse?> = _faceInfo

    private val _remainingMatchTickets = MutableStateFlow<Int?>(null)
    val remainingMatchTickets: StateFlow<Int?> = _remainingMatchTickets

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadProfileData() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val faceResult = getMyFaceInfoUseCase()
                val ticketResult = getMatchRemainUseCase()

                faceResult.onSuccess { _faceInfo.value = it }
                    .onFailure { _error.value = it.message }

                ticketResult.onSuccess { _remainingMatchTickets.value = it }
                    .onFailure { _error.value = it.message }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
