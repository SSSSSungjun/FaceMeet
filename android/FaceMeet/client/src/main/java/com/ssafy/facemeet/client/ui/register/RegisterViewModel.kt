package com.ssafy.facemeet.client.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.client.navigation.setting.RegisterMode
import com.ssafy.facemeet.client.ui.map.MapDataStore
import com.ssafy.facemeet.client.ui.register.model.RegUiState
import com.ssafy.facemeet.client.ui.register.model.RegisterNaviEvent
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.data.remote.dto.request.UserInfoModRequest
import com.ssafy.facemeet.core.domain.usecase.GetUserInfoUseCase
import com.ssafy.facemeet.core.domain.usecase.OnboardingUseCase
import com.ssafy.facemeet.core.domain.usecase.UpdateUserInfoUseCase
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
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _naviEvent = MutableSharedFlow<RegisterNaviEvent?>()
    val naviEvent: SharedFlow<RegisterNaviEvent?> = _naviEvent

    private val _uiState = MutableStateFlow(RegUiState())
    val uiState: StateFlow<RegUiState> = _uiState

    fun navigateToNext() = viewModelScope.launch {
        _naviEvent.emit(RegisterNaviEvent.ToCamera)
    }

    fun navigateToMap() = viewModelScope.launch {
        _naviEvent.emit(RegisterNaviEvent.ToMap)
    }

    fun navigateToBack(mode: RegisterMode) = viewModelScope.launch {
        if (mode == RegisterMode.REGISTER) {
            tokenManager.clearTokens()
            mapDataStore.clear()
        }
        _naviEvent.emit(RegisterNaviEvent.ToBack)
    }

    // 최초에 기존 정보 불러오기
    fun loadInitial(mode: RegisterMode) = viewModelScope.launch {
        when (mode) {
            RegisterMode.REGISTER -> updateAddressFromStore()
            RegisterMode.EDIT -> getUserInfoUseCase().onSuccess { user ->
                _uiState.update {
                    it.copy(
                        nickname = user.nickname,
                        selectedAddress = user.address,
                        selectedAgeRange = user.preferAgeLower..user.preferAgeUpper,
                        hasLocation = true // 좌표는 mapDataStore로 관리
                    )
                }
                // 필요하면 GET 응답 좌표를 mapDataStore에 동기화
                mapDataStore.setLocation(user.latitude, user.longitude, user.address)
            }
        }
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


//    fun submitRegistration() {
//        val state = _uiState.value
//        if (state.isValid()) {
//            viewModelScope.launch {
//                onBoarding()
//            }
//        }
//    }
//
//    suspend fun onBoarding() {
//        val request = OnboardingRequest(
//            nickname = _uiState.value.nickname,
//            address = mapDataStore.address,
//            latitude = mapDataStore.latitude,
//            longitude = mapDataStore.longitude,
//            preferAgeLower = _uiState.value.selectedAgeRange.first,
//            preferAgeUpper = _uiState.value.selectedAgeRange.last
//        )
//        onboardingUseCase.invoke(request)
//    }

    /** 등록/수정 공통 처리 */
    fun submit(mode: RegisterMode) = viewModelScope.launch {
        val state = _uiState.value
        if (!state.isValid()) return@launch

        when (mode) {
            RegisterMode.REGISTER -> {
                val req = OnboardingRequest(
                    nickname = state.nickname,
                    address = mapDataStore.address ?: "",
                    latitude = mapDataStore.latitude ?: 0.0,
                    longitude = mapDataStore.longitude ?: 0.0,
                    preferAgeLower = state.selectedAgeRange.first,
                    preferAgeUpper = state.selectedAgeRange.last
                )
                onboardingUseCase(req)
            }

            RegisterMode.EDIT -> {
                val req = UserInfoModRequest(
                    nickname = state.nickname,
                    address = state.selectedAddress,
                    latitude = mapDataStore.latitude ?: 0.0,
                    longitude = mapDataStore.longitude ?: 0.0,
                    preferAgeLower = state.selectedAgeRange.first,
                    preferAgeUpper = state.selectedAgeRange.last
                )
                updateUserInfoUseCase(req)
            }
        }
    }
}
