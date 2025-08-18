package com.ssafy.facemeet.client.ui.mainmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.domain.usecase.GetHomeInfoUseCase
import com.ssafy.facemeet.core.domain.usecase.GetUnreadNotificationCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val getHomeInfoUseCase: GetHomeInfoUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase
) : ViewModel() {

    data class UiState(
        val img: String? = null,
        val nickname: String = "",
        val title: String = "",
        val remainingMatchTickets: Int = 0,
        val unreadCount: Int = 0,
        val loading: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadHome() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        try {
            val result = getHomeInfoUseCase()
            result.onSuccess { home ->
                _uiState.value = _uiState.value.copy(
                    img = home.img,
                    nickname = home.nickname,
                    title = home.title,
                    remainingMatchTickets = home.remainingMatchTickets,
                    unreadCount = home.unreadCount,
                    loading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(loading = false, error = e.message)
        }
    }

    fun refreshUnreadCount() = viewModelScope.launch {
        getUnreadNotificationCountUseCase()
            .onSuccess { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
            .onFailure {
                // 뱃지 정도는 실패 시 0으로 폴백
                _uiState.update { it.copy(unreadCount = 0) }
            }
    }

}
