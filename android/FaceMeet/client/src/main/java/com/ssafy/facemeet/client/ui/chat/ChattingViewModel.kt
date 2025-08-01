package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChattingViewModel @Inject constructor(
    private val webSocketManager : ChatWebSocketManager,
    private val tokenManager: TokenManager
): ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    // WebSocket 메시지와 연결 상태 관찰
    val messages: LiveData<List<ChatMessageItem>> = webSocketManager.messages
    val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

    fun connectToChat(userId: Long, roomId: Int, receiverId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    currentUserId = userId,
                    roomId = roomId,
                    receiverId = receiverId,
                    isLoading = true
                )

                val token = tokenManager.getAccessToken()
                // WebSocket 연결
                if(token!=null)
                    webSocketManager.connect(userId, token)
                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateMessageText(text: String) {
        _uiState.value = _uiState.value.copy(
            messageText = text,
            canSendMessage = text.isNotBlank()
        )
    }


    fun sendMessage(content: String) {
        val state = _uiState.value
        if (content.isBlank() || state.currentUserId == 0L) return

        viewModelScope.launch {
            try {
                webSocketManager.sendMessage(
                    content = content.trim(),
                    roomId = state.roomId,
                    senderId = state.currentUserId,
                    receiverId = state.receiverId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun markAsRead() {
        val state = _uiState.value
        if (state.currentUserId != 0L && state.roomId != 0) {
            webSocketManager.markAsRead(state.roomId.toString(), state.currentUserId)
        }
    }

    fun updateConnectionState(connectionState: ConnectionState) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            canSendMessage = currentState.messageText.isNotBlank() &&
                    connectionState == ConnectionState.CONNECTED
        )
    }

    fun endChatRoom() {
        webSocketManager.endChatRoom()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}