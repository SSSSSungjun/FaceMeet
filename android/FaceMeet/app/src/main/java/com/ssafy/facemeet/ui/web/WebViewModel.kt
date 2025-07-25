package com.ssafy.facemeet.ui.web

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "WebViewModel"
@HiltViewModel
class WebViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // 카카오 로그인 성공시 토큰 저장
    fun saveTokens(accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "토큰 저장 시작: accessToken=$accessToken, refreshToken=$refreshToken")
                tokenManager.saveTokens(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )

                Log.d(TAG, "토큰 저장 완료")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccess = true,
                    error = null
                )

            } catch (e: Exception) {
                Log.e(TAG, "토큰 저장 실패", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccess = false,
                    error = "토큰 저장에 실패했습니다: ${e.message}"
                )
            }
        }
    }

    fun onLoginStart() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            isLoginSuccess = false
        )
    }

    fun onLoginError(error: String) {
        Log.e(TAG, "로그인 에러: $error")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = error,
            isLoginSuccess = false
        )
    }

    fun onLoginCancel() {
        Log.d(TAG, "로그인 취소")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = null,
            isLoginSuccess = false
        )
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val error: String? = null
)