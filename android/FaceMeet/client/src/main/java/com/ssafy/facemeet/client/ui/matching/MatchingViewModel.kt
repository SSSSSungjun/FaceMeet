package com.ssafy.facemeet.client.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ssafy.facemeet.core.data.remote.datasource.MatchRemoteDataSource
import com.ssafy.facemeet.core.data.remote.dto.response.ApiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

private const val TAG = "MatchingViewModel"

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val matchRemoteDataSource: MatchRemoteDataSource
) : ViewModel() {

    private val _matchedChatRoomId = MutableStateFlow<Long?>(null)
    val matchedChatRoomId: StateFlow<Long?> = _matchedChatRoomId

    private val _remainingMatchTickets = MutableStateFlow<Int?>(null)
    val remainingMatchTickets: StateFlow<Int?> = _remainingMatchTickets

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadRemainingMatchTickets() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = matchRemoteDataSource.getMatchRemain()
                if (res.isSuccessful) {
                    _remainingMatchTickets.value = res.body()
                } else {
                    _error.value = "HTTP ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류가 발생했어요."
            } finally {
                _loading.value = false
            }
        }
    }

    /** 매칭 시작 */
    fun startMatching() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = matchRemoteDataSource.getMatch()
                _matchedChatRoomId.value = response.chatRoomId.toLong()
                _remainingMatchTickets.value =
                    (_remainingMatchTickets.value ?: 0).let { if (it > 0) it - 1 else 0 }
            } catch (e: HttpException) {
                val raw = e.response()?.errorBody()?.string()
                val apiErr = parseApiError(raw)
                _error.value = apiErr?.message ?: "HTTP ${e.code()}"
            } catch (e: Exception) {
                _error.value = e.message ?: "매칭 중 오류가 발생했어요."
            } finally {
                _loading.value = false
            }
        }
    }


    fun resetMatchingResult() {
        _matchedChatRoomId.value = null
    }

    fun clearError() {
        _error.value = null
    }
}


private val gson = Gson()

private fun parseApiError(json: String?): ApiError? =
    try {
        if (json.isNullOrBlank()) null else gson.fromJson(json, ApiError::class.java)
    } catch (_: JsonSyntaxException) {
        null
    }