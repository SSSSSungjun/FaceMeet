package com.ssafy.facemeet.client.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.client.ui.map.MapDataStore
import com.ssafy.facemeet.client.ui.register.model.RegUiState
import com.ssafy.facemeet.client.ui.register.model.RegisterNaviEvent
import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.domain.usecase.OnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RegisterViewModel"

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val mapDataStore: MapDataStore,
    private val onboardingUseCase: OnboardingUseCase,
) : ViewModel() {

    private val _naviEvent = MutableSharedFlow<RegisterNaviEvent?>()
    val naviEvent: SharedFlow<RegisterNaviEvent?> = _naviEvent

    private val _uiState = MutableStateFlow(RegUiState())
    val uiState: StateFlow<RegUiState> = _uiState

    fun navigateToCamera() = viewModelScope.launch {
        _naviEvent.emit(RegisterNaviEvent.ToCamera)
    }

    fun navigateToMap() = viewModelScope.launch {
        _naviEvent.emit(RegisterNaviEvent.ToMap)
    }

    fun navigateToBack() = viewModelScope.launch {
        _naviEvent.emit(RegisterNaviEvent.ToBack)
    }

    fun updateAddressFromStore() {
        _uiState.update {
            it.copy(
                selectedAddress = mapDataStore.address?.takeIf { it.isNotBlank() } ?: "",
                hasLocation = mapDataStore.hasLocation()
            )
        }
    }

    fun updateNickname(nickname: String) {
        _uiState.update { it.copy(nickname = nickname) }
    }

    fun updateAgeRange(minAge: Int, maxAge: Int) {
        _uiState.update { it.copy(selectedAgeRange = minAge..maxAge) }
    }

    fun submitRegistration() {
        val state = _uiState.value
        if (state.isValid()) {
            viewModelScope.launch {
                onBoarding()
                mapDataStore.clear()
            }
        }
    }

    suspend fun onBoarding() {
        val request = OnboardingRequest(
            nickname = _uiState.value.nickname,
            address = mapDataStore.address,
            latitude = mapDataStore.latitude,
            longitude = mapDataStore.longitude,
            preferAgeLower = _uiState.value.selectedAgeRange.first,
            preferAgeUpper = _uiState.value.selectedAgeRange.last
        )
        onboardingUseCase.invoke(request)
    }

}
