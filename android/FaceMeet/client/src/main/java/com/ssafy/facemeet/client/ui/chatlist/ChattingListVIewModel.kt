package com.ssafy.facemeet.client.ui.chatlist

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.domain.usecase.GetChattingListUseCase
import com.ssafy.facemeet.core.domain.usecase.PostChattingLeaveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChattingListViewModel"

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ChattingListViewModel @Inject constructor(
    private val chatWebSocketManager: ChatWebSocketManager,
    private val tokenManager: TokenManager,
    private val getChattingListUseCase: GetChattingListUseCase,
    private val postChattingLeaveUseCase: PostChattingLeaveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState

    internal var selectedRoomId: Long = 0L

    init {
        viewModelScope.launch {
            chatWebSocketManager.connect(0L, tokenManager.getAccessToken().toString(), 0L)
            chatWebSocketManager.onNewMessageForList = {
                Log.d(TAG, "✅ onNewMessageForList 콜백 설정 완료!")
                loadChattingList()
            }
            loadChattingList()
        }
    }

    fun loadChattingList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getChattingListUseCase().onSuccess { chatList ->
                Log.d(TAG, "loadChattingList: 로드 성공 $chatList")
                _uiState.value = _uiState.value.copy(
                    chatList = chatList,
                    isLoading = true
                )
            }.onFailure {
                Log.d(TAG, "${it.message} 방 리스트 불러오기 실패")
            }
        }
    }

    fun clickExitRoom() {
        _uiState.value = _uiState.value.copy(showExitDialog = true)
    }

    fun dismissExitDialog() {
        _uiState.value = _uiState.value.copy(showExitDialog = false)
    }

    fun setSelectedRoomId(roomId: Long) {
        selectedRoomId = roomId
    }

    fun exitRoom(chatRoomId: Long) {
        viewModelScope.launch {
            postChattingLeaveUseCase(chatRoomId).onSuccess {
                Log.d(TAG, "방 나가기 성공")

            }.onFailure {
                Log.d(TAG, "방 나가기 실패")
            }
        }
    }
}