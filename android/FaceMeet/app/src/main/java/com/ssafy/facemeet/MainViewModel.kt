package com.ssafy.facemeet

import android.util.Log
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

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isLoggedIn: StateFlow<Boolean?> = tokenManager.isLoggedInFlow()
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)


    private val _pendingNavigation = MutableStateFlow<Pair<String, Long?>?>(null)
    val pendingNavigation: StateFlow<Pair<String, Long?>?> = _pendingNavigation.asStateFlow()

    fun setPendingNavigation(screen: String, roomId: Long?) {
        Log.d("MainViewModel", "✅ setPendingNavigation: $screen, $roomId")
        _pendingNavigation.value = screen to roomId
    }

    fun clearPendingNavigation() {
        _pendingNavigation.value = null
    }

}

