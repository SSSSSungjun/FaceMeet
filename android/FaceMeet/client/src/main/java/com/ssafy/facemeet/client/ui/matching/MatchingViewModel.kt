package com.ssafy.facemeet.client.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.datasource.MatchRemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MatchingViewModel"

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val matchRemoteDataSource: MatchRemoteDataSource
) : ViewModel() {

    private val _matchedChatRoomId = MutableStateFlow<Long?>(null)
    val matchedChatRoomId: StateFlow<Long?> = _matchedChatRoomId

    // 남은 매칭권
    private val _remainingMatchTickets = MutableStateFlow<Int?>(null)
    val remainingMatchTickets: StateFlow<Int?> = _remainingMatchTickets

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /** 남은 매칭권 불러오기 */
    fun loadRemainingMatchTickets() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = matchRemoteDataSource.getMatchRemain() // Response<Int>
                if (res.isSuccessful) {
                    _remainingMatchTickets.value = res.body()
                } else {
                    _error.value = "HTTP ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /** 매칭 시작 */
    fun startMatching() {
        viewModelScope.launch {
            try {
                val response = matchRemoteDataSource.getMatch() // 매칭 시작 API (기존)
                _matchedChatRoomId.value = response.chatRoomId.toLong()

                // 성공 시 로컬 카운트도 1 감소(서버 정답 대로 다시 불러오고 싶으면 아래 줄 대신 loadRemainingMatchTickets() 호출)
                _remainingMatchTickets.value =
                    (_remainingMatchTickets.value ?: 0).let { if (it > 0) it - 1 else 0 }
            } catch (e: Exception) {
                _matchedChatRoomId.value = 0
            }
        }
    }

    fun resetMatchingResult() {
        _matchedChatRoomId.value = null
    }
}
