package com.ssafy.facemeet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isLoggedIn: StateFlow<Boolean?> = tokenManager.isLoggedInFlow()
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    sealed class PendingNav {
        data object None : PendingNav()
        data class Chat(val roomId: Long) : PendingNav()
        data class TicketEvent(val settingId: Long? = null) : PendingNav()
        data object NotificationCenter : PendingNav()
        data object Start : PendingNav() // 시작화면으로
    }

    private val _pendingNav = MutableStateFlow<PendingNav>(PendingNav.None)
    val pendingNav: StateFlow<PendingNav> = _pendingNav.asStateFlow()

    fun goToChat(roomId: Long) {
        _pendingNav.value = PendingNav.Chat(roomId)
    }

    fun goToTicketEvent(settingId: Long? = null) {
        _pendingNav.value = PendingNav.TicketEvent(settingId)
    }

    fun goToNotificationCenter() {
        _pendingNav.value = PendingNav.NotificationCenter
    }

    fun consumePendingNavigation() {
        _pendingNav.value = PendingNav.None
    }

    fun logout() {
        viewModelScope.launch {
            // suspend 버전이 있으면 우선 사용하고, 없으면 sync로 폴백
            runCatching {
                // 예: tokenManager.clearTokens() 가 suspend 라면 이걸 사용
                tokenManager.clearTokens()
            }.recoverCatching {
                // 프로젝트에 clearTokensSync()만 있으면 여기서 처리
                tokenManager.clearTokensSync()
            }
            // 필요하면 isLoading 초기화 등 부가 상태 리셋
            _pendingNav.value = PendingNav.Start
        }
    }

}
