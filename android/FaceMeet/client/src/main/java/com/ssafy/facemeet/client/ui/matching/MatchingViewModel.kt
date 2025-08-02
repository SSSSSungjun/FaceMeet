package com.ssafy.facemeet.client.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchingViewModel @Inject constructor() : ViewModel() {

    private val _matchingResult = MutableStateFlow<String?>(null)
    val matchingResult: StateFlow<String?> = _matchingResult

    fun startMatching() {
        viewModelScope.launch {
            // TODO: 실제 API 요청
            delay(3000) // 시뮬레이션용

            _matchingResult.value = "아이디들어갈값" // API 결과를 emit
        }
    }

    fun resetMatchingResult() {
        _matchingResult.value = null
    }
}
