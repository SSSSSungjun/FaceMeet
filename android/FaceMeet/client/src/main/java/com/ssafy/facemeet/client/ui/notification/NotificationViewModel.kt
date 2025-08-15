package com.ssafy.facemeet.client.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.dto.response.NotificationResponse
import com.ssafy.facemeet.core.domain.usecase.GetNotificationsUseCase
import com.ssafy.facemeet.core.domain.usecase.GetUnreadNotificationCountUseCase
import com.ssafy.facemeet.core.domain.usecase.ReadNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val loading: Boolean = true,
    val items: List<NotificationResponse> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val readNotification: ReadNotificationUseCase,
    private val getUnreadCount: GetUnreadNotificationCountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            // 목록
            getNotifications()
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        loading = false,
                        items = list
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "알림을 불러오지 못했습니다."
                    )
                }

            // 안읽음 count (실패해도 화면은 유지)
            getUnreadCount()
                .onSuccess { count -> _state.value = _state.value.copy(unreadCount = count) }
                .onFailure { /* 무시하거나 로그만 */ }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            readNotification(id)
                .onSuccess {
                    // 성공 시 UI 반영(해당 알림 isRead=true로 업데이트)
                    val updated = _state.value.items.map {
                        if (it.notificationId == id) it.copy(isRead = true) else it
                    }
                    _state.value = _state.value.copy(items = updated)
                    // 카운트 재조회(선택사항)
                    getUnreadCount().onSuccess { c ->
                        _state.value = _state.value.copy(unreadCount = c)
                    }
                }
                .onFailure { /* 실패 시 토스트/스낵바는 화면 계층에서 처리 */ }
        }
    }
}
