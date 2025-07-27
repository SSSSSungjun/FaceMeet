package com.ssafy.facemeet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainViewModel"

    @HiltViewModel
    class MainViewModel @Inject constructor(
        private val tokenManager: TokenManager
    ) : ViewModel() {
        @Volatile
        private var _isAppReady = false // 일반 변수로 관리
        val isAppReady get() = _isAppReady

        private val _isLoading = MutableStateFlow(true)
        val isLoading = _isLoading.asStateFlow()

        fun initializeApp() {
            viewModelScope.launch {
                tokenManager.initializeCache()
                delay(300)
                _isLoading.value = false
                _isAppReady = true
            }
        }

    }

