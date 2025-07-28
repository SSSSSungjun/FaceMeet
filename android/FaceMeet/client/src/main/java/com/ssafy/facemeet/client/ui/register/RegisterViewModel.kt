package com.ssafy.facemeet.client.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel () {

    private val _naviEvent = MutableSharedFlow<RegisterNaviEvent?>()
    val naviEvent: SharedFlow<RegisterNaviEvent?> = _naviEvent

    fun navigateToCamera() {
        viewModelScope.launch {
            _naviEvent.emit(RegisterNaviEvent.ToCamera)
        }
    }

    fun navigateToMap() {
        viewModelScope.launch {
            _naviEvent.emit(RegisterNaviEvent.ToMap)
        }
    }

    fun navigateToBack() {
        viewModelScope.launch {
            _naviEvent.emit(RegisterNaviEvent.ToBack)
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            try {
                tokenManager.saveTokens(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )

            } catch (e: Exception) {
            }
        }
    }

}
