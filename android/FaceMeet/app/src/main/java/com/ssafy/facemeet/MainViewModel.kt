package com.ssafy.facemeet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.constant.AuthStatus
import com.ssafy.facemeet.core.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Compose용 State 사용
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null) // null = 로딩중
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    init {
        checkInitialAuthStatus()
    }

    private fun checkInitialAuthStatus() {
        viewModelScope.launch {
            try {
                val authStatus = authRepository.checkAuthStatus()
                _isLoggedIn.value = authStatus == AuthStatus.LOGGED_IN
            } catch (e: Exception) {
                _isLoggedIn.value = false
            }
        }
    }

    // 웹뷰에서 토큰 받았을 때
    fun saveTokensFromWebView(accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            authRepository.saveTokens(accessToken, refreshToken)
            _isLoggedIn.value = true
        }
    }

}
