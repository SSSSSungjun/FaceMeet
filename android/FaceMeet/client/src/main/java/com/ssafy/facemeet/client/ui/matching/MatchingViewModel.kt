package com.ssafy.facemeet.client.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.datasource.MatchRemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val matchRemoteDataSource: MatchRemoteDataSource
) : ViewModel() {

    private val _matchedChatRoomId = MutableStateFlow<Long?>(null)
    val matchedChatRoomId: StateFlow<Long?> = _matchedChatRoomId

    fun startMatching() {
        viewModelScope.launch {
            try {
                val response = matchRemoteDataSource.getMatch()
                _matchedChatRoomId.value = response.chatRoomId.toLong()
            } catch (e: Exception) {
                _matchedChatRoomId.value = 0
            }
        }
    }

    fun resetMatchingResult() {
        _matchedChatRoomId.value = null
    }
}
