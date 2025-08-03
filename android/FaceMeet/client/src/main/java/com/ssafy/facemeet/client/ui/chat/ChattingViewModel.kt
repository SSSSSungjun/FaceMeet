package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesAllUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChattingViewModel"


@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChattingViewModel @Inject constructor(
    private val webSocketManager: ChatWebSocketManager,
    private val tokenManager: TokenManager,
    private val getChattingMessagesAllUseCase: GetChattingMessagesAllUseCase
) : ViewModel() {

    var currentUserId: Long = 0L

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
    val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

    val messages: LiveData<List<ChatMessageItem>> = webSocketManager.messages
    val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

    init {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
            Log.d("ChattingViewModel", "현재 사용자 ID: $currentUserId")
        }
    }

    fun navigateToBack() {
        viewModelScope.launch {
            _naviEvent.emit(ChatNaviEvent.ToBack)
        }
    }

    fun getAllChattingMessage(roomId: Long) {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            getChattingMessagesAllUseCase.invoke(roomId).onSuccess { chattingAll ->
                val existingMessages = chattingAll.messages.map { chatElement ->
                    ChatMessageItem(
                        chatElement = chatElement,
                        messageType = MessageType.TEXT
                    )
                }

                webSocketManager.setInitialMessages(existingMessages)

                _uiState.update {
                    it.copy(
                        roomId = roomId,
                        isLoading = false
                    )
                }
            }.onFailure {
                Log.e(TAG, "getAllChattingMessageError", it) // 전체 예외 로그
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun connectToChat(roomId: Long, receiverId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        currentUserId = currentUserId,
                        roomId = roomId,
                        receiverId = receiverId,
                        isLoading = true
                    )
                }

                val token = tokenManager.getAccessToken()

                if (token != null) {
                    webSocketManager.connect(currentUserId, token)
                }
                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun updateMessageText(text: String) {
        _uiState.update {
            it.copy(
                messageText = text,
                canSendMessage = text.isNotBlank()
            )
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        if (!state.canSendMessage || state.currentUserId == 0L) return

        viewModelScope.launch {
            try {
                webSocketManager.sendMessage(
                    content = state.messageText.trim(),
                    roomId = state.roomId,
                    senderId = state.currentUserId,
                    receiverId = state.receiverId
                )
                _uiState.update {
                    it.copy(
                        messageText = "",
                        canSendMessage = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun markAsRead() {
        val state = _uiState.value
        if (state.currentUserId != 0L && state.roomId != 0L) {
            webSocketManager.markAsRead(
                roomId = state.roomId.toString(),
                userId = state.currentUserId,
                senderId = state.receiverId
            )
        }
    }

    fun updateConnectionState(connectionState: ConnectionState) {
        val currentState = _uiState.value
        _uiState.update {
            currentState.copy(
                canSendMessage = currentState.messageText.isNotBlank() &&
                        connectionState == ConnectionState.CONNECTED
            )
        }
    }

    fun endChatRoom() {
        webSocketManager.disconnect()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
        webSocketManager.messages.removeObserver { }
    }
}
