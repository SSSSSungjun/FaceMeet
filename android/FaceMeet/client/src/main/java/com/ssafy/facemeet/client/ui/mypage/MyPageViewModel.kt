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

    sealed interface UiEvent {
        data class Toast(val message: String) : UiEvent
    }

    private val _event = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<UiEvent> = _event

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
        // 연타 방지(원하면 제거 가능)
        if (uiState.value.isTogglingMarketing) return

        viewModelScope.launch {
            val prev = uiState.value.userInfo.isEventSubscribed

            // 1) 낙관적 업데이트 + 즉시 토스트
            _uiState.update { s ->
                s.copy(
                    userInfo = s.userInfo.copy(isEventSubscribed = enabled),
                    isTogglingMarketing = true,
                    error = null
                )
            }
            _event.tryEmit(UiEvent.Toast(if (enabled) "마케팅 알림을 켰어요" else "마케팅 알림을 껐어요"))

            // 2) 서버 반영
            runCatching {
                if (enabled) postSubscriptionUseCase.invoke(TOPIC.ONE.value)
                else deleteSubscriptionUseCase.invoke(TOPIC.ONE.value)
            }.onSuccess {
                // 3) 성공: 끝 (상태 이미 반영됨)
                _uiState.update { it.copy(isTogglingMarketing = false) }
            }.onFailure { e ->
                // 4) 실패: 롤백 + 실패 토스트
                _uiState.update { s ->
                    s.copy(
                        userInfo = s.userInfo.copy(isEventSubscribed = prev),
                        isTogglingMarketing = false,
                        error = e.message
                    )
                }
                _event.tryEmit(UiEvent.Toast("변경에 실패하여 되돌렸어요"))
            }
        }
    }


    fun clickWithdrawBtn() = _uiState.update { it.copy(isWithdrawBtnClicked = true) }
    fun dismissWithdrawDialog() = _uiState.update { it.copy(isWithdrawBtnClicked = false) }

    fun loadUserProfile() = viewModelScope.launch {
        userInfoUserUseCase()
            .onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, userInfo = user, error = null) }
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
                tokenManager.clearTokens()
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

enum class TOPIC(val value: Long) {
    ONE(1),
}