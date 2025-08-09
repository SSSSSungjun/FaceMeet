package com.ssafy.facemeet.client.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.client.ui.mypage.model.MyPageNaviEvent
import com.ssafy.facemeet.client.ui.mypage.model.MyPageUiState
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.domain.usecase.DeleteSubscriptionUseCase
import com.ssafy.facemeet.core.domain.usecase.DeleteUserUseCase
import com.ssafy.facemeet.core.domain.usecase.GetDeviceTokensUseCase
import com.ssafy.facemeet.core.domain.usecase.GetUserInfoUseCase
import com.ssafy.facemeet.core.domain.usecase.LogoutUseCase
import com.ssafy.facemeet.core.domain.usecase.PostSubscriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyPageViewModel"

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userInfoUserUseCase: GetUserInfoUseCase,
    private val tokenManager: TokenManager,
    private val userLogoutUseCase: LogoutUseCase,
    private val userDeleteUserUseCase: DeleteUserUseCase,
    private val postSubscriptionUseCase: PostSubscriptionUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val getDeviceTokensUseCase: GetDeviceTokensUseCase

) : ViewModel() {
    private var fcmTokenId: Long? = null

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private val _naviEvent = MutableSharedFlow<MyPageNaviEvent?>()
    val naviEvent: SharedFlow<MyPageNaviEvent?> = _naviEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            getDeviceTokensUseCase().onSuccess { tokenInfo ->
                val androidToken = tokenInfo.firstOrNull { it.deviceType == "android" }
                if (androidToken != null) {
                    fcmTokenId = androidToken.tokenId
                    Log.d(TAG, "단일 Android 토큰: $fcmTokenId")
                } else {
                    Log.e(TAG, "Android 토큰을 찾을 수 없습니다.")
                }
            }.onFailure {
                Log.d(TAG, "fcm: ${it.message} ")
            }
        }
    }

    fun navigateToLogout() {
        viewModelScope.launch {
            val success = logout()
            if (success) {
                Log.d(TAG, "navigateToLogout: 로그 아웃")
                _naviEvent.emit(MyPageNaviEvent.ToLogout)
            }

        }
    }

    fun navigateToModify() {
        viewModelScope.launch {
            _naviEvent.emit(MyPageNaviEvent.ToModify)
        }
    }

    fun navigateToWithdraw() {
        viewModelScope.launch {
            val success = withdraw()
            if (success) {
                Log.d(TAG, "navigateToWithdraw: 회원 탈퇴")
                _naviEvent.emit(MyPageNaviEvent.ToWithdraw)
            }
        }
    }

    fun setMarketingAlarm(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(marketingAlarmEnabled = enabled)
            }

            if (uiState.value.marketingAlarmEnabled) {
                Log.d(TAG, "setMarketingAlarm: 알림 설정")
                postSubscriptionUseCase.invoke(fcmTokenId?.toLong() ?: 0L)

            } else {
                Log.d(TAG, "setMarketingAlarm: 알림 해제")
                deleteSubscriptionUseCase.invoke(fcmTokenId?.toLong() ?:0L)
            }
        }

    }


    fun clickWithdrawBtn() = _uiState.update { it.copy(isWithdrawBtnClicked = true) }
    fun dismissWithdrawDialog() = _uiState.update { it.copy(isWithdrawBtnClicked = false) }

    fun loadUserProfile() = viewModelScope.launch {
        userInfoUserUseCase()
            .onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, userProfile = user, error = null) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "사용자 정보를 불러올 수 없습니다.") }
            }
    }

    suspend fun logout(): Boolean {
        return try {
            userLogoutUseCase().onSuccess {
                tokenManager.clearTokens()
            }.onFailure { e ->
                Log.d(TAG, "logout: ${e.message}")
            }.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    suspend fun withdraw(): Boolean {
        return try {
            userDeleteUserUseCase().onSuccess {
                tokenManager.clearTokens()
            }.onFailure { e ->
                Log.d(TAG, "logout: ${e.message}")
            }.isSuccess
        } catch (e: Exception) {
            false
        }
    }

}