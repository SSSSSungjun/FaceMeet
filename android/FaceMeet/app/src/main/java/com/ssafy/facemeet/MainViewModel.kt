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
}
