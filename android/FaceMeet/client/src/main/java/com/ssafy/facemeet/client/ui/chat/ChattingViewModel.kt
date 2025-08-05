package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.domain.model.ChatRoom
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesCurrentUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesLastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

private const val TAG = "ChattingViewModel"
private const val TEMP_MESSAGE_TIMEOUT = 10000L
private const val REALTIME_MESSAGE_LIMIT = 50


@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChattingViewModel @Inject constructor(
    private val webSocketManager: ChatWebSocketManager,
    private val tokenManager: TokenManager,
    private val getChattingMessagesLastUseCase: GetChattingMessagesLastUseCase,
    private val getChattingMessagesCurrentUseCase: GetChattingMessagesCurrentUseCase
) : ViewModel() {

    var currentUserId: Long = 0L

    // 상태 통합
    private val _messageState = MutableStateFlow(MessageState())
    val messageState: StateFlow<MessageState> = _messageState

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    // 이벤트들
    private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
    val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

    private val _scrollEvent = MutableSharedFlow<ScrollEvent>()
    val scrollEvent: SharedFlow<ScrollEvent> = _scrollEvent.asSharedFlow()

    val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

    // 임시 메시지 관리
    private data class TempMessage(
        val message: ChatMessageItem,
        val localId: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _tempMessages = MutableStateFlow<List<TempMessage>>(emptyList())

    init {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }
    }

    private val _realtimeMessages = MutableStateFlow<List<ChatMessageItem>>(emptyList())

    val liveMessages: StateFlow<List<ChatMessageItem>> = combine(
        _tempMessages,
        _realtimeMessages
    ) { tempMessages, realtimeMessages ->
        (tempMessages.map { it.message } + realtimeMessages)
            .sortedByDescending { it.chatElement.createdAt } // 최신순 정렬
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun initializeChat(roomId: Long) {
        webSocketManager.disconnect()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            loadChatRoomInfo(roomId)
            loadInitialMessages(roomId)
            setupWebSocketCallbacks()
            connectToChat()
        }
    }

    private fun loadInitialMessages(roomId: Long) {
        viewModelScope.launch {
            val pagingFlow = getChattingMessagesCurrentUseCase.invoke(roomId)
                .cachedIn(viewModelScope)

            _messageState.update {
                it.copy(pagedMessages = pagingFlow)
            }
            _tempMessages.value = emptyList()
            _realtimeMessages.value = emptyList()

            Log.d(TAG, "초기 메시지 로드 완료")
        }
    }

    private fun setupWebSocketCallbacks() {
        webSocketManager.setOnNewMessageCallback { newMessage ->
            handleNewMessage(newMessage)
        }
    }

    private fun handleNewMessage(newMessage: ChatMessageItem) {
        viewModelScope.launch {
            if (newMessage.chatElement.senderID == currentUserId) {
                removeTempMessage(newMessage)
            }

            _realtimeMessages.update { currentList ->
                currentList + newMessage
            }

            if (_uiState.value.scrollState.isAtBottom) {
                triggerScroll(ScrollEvent.ToBottom)
            }

            cleanupMessagesIfNeeded()

            Log.d(TAG, "새 메시지 처리 완료: ${newMessage.chatElement.content}")
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        if (!state.canSendMessage || currentUserId == 0L) return

        val messageContent = state.messageText.trim()
        val localId = generateLocalId(messageContent)

        // 임시 메시지 생성
        val tempMessage = createTempMessage(messageContent, state.roomInfo, localId)
        addTempMessage(tempMessage)

        // UI 상태 업데이트
        _uiState.update {
            it.copy(
                messageText = "",
                canSendMessage = false,
                scrollState = it.scrollState.copy(isAtBottom = true)
            )
        }

        // 자동 스크롤
        triggerScroll(ScrollEvent.ToBottom)

        // 웹소켓으로 메시지 전송
        viewModelScope.launch {
            try {
                webSocketManager.sendMessage(
                    content = messageContent,
                    roomId = state.roomInfo.chatRoomID,
                    senderId = currentUserId,
                    receiverId = state.roomInfo.partnerID
                )

                // 타임아웃 후 임시 메시지 제거
                delay(TEMP_MESSAGE_TIMEOUT)
                removeTempMessageById(localId)

            } catch (e: Exception) {
                removeTempMessageById(localId)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // 스크롤 관련 메서드들
    fun onScrollStateChanged(
        isScrolling: Boolean,
        firstVisibleItemIndex: Int,
        totalItemCount: Int
    ) {
        val isAtBottom = firstVisibleItemIndex <= 2

        _uiState.update {
            it.copy(
                scrollState = it.scrollState.copy(
                    isUserScrolling = isScrolling,
                    isAtBottom = isAtBottom
                )
            )
        }
    }

    fun onInitialLoadComplete() {
        triggerScroll(ScrollEvent.ToBottom)
    }

    fun onKeyboardShown() {
        if (_uiState.value.scrollState.isAtBottom) {
            triggerScroll(ScrollEvent.WithKeyboard)
        }
    }

    fun scrollToBottomManually() {
        _uiState.update {
            it.copy(scrollState = it.scrollState.copy(isAtBottom = true))
        }
        triggerScroll(ScrollEvent.ToBottom)
    }

    private fun triggerScroll(event: ScrollEvent) {
        viewModelScope.launch {
            _scrollEvent.emit(event)
        }
    }

    // 메시지 정리
    private fun cleanupMessagesIfNeeded() {
        val currentCount = _realtimeMessages.value.size
        if (currentCount > REALTIME_MESSAGE_LIMIT) {
            Log.d(TAG, "메시지 정리 시작: ${currentCount}개")
            refreshMessages()
        }
    }

    fun refreshMessages() {
        viewModelScope.launch {
            val currentRoomId = _uiState.value.roomInfo.chatRoomID
            if (currentRoomId != 0L) {
                val newPagingFlow = getChattingMessagesCurrentUseCase.invoke(currentRoomId)
                    .cachedIn(viewModelScope)

                _messageState.update {
                    it.copy(pagedMessages = newPagingFlow)
                }
                _realtimeMessages.value = emptyList()
            }
        }
    }

    // 유틸리티 메서드들
    private fun generateLocalId(content: String): String {
        return "${currentUserId}_${System.currentTimeMillis()}_${content.hashCode()}"
    }

    private fun createTempMessage(content: String, roomInfo: ChatRoom, localId: String): TempMessage {
        val chatElement = ChatElement(
            content = content,
            senderID = currentUserId,
            receiverID = roomInfo.partnerID,
            roomID = roomInfo.chatRoomID,
            createdAt = ZonedDateTime.now().toString(),
            isRead = false,
            readAt = ""
        )

        val messageItem = ChatMessageItem(
            chatElement = chatElement,
            messageType = MessageType.TEXT
        )

        return TempMessage(
            message = messageItem,
            localId = localId
        )
    }

    private fun addTempMessage(tempMessage: TempMessage) {
        _tempMessages.update { current -> current + tempMessage }
    }

    private fun removeTempMessage(realMessage: ChatMessageItem) {
        _tempMessages.update { currentList ->
            currentList.filter { temp ->
                !(temp.message.chatElement.content == realMessage.chatElement.content &&
                        temp.message.chatElement.senderID == realMessage.chatElement.senderID &&
                        temp.message.chatElement.roomID == realMessage.chatElement.roomID)
            }
        }
    }

    private fun removeTempMessageById(localId: String) {
        _tempMessages.update { current ->
            current.filter { it.localId != localId }
        }
    }

    //notice
    fun toggleNotice() {
        _uiState.update { it.copy(isNoticeOpen = !it.isNoticeOpen) }
    }

    // 기타 메서드들
    suspend fun loadChatRoomInfo(roomId: Long) {
        getChattingMessagesLastUseCase.invoke(roomId, 20).onSuccess { chattingAll ->
            _uiState.update { it.copy(roomInfo = chattingAll.chatRoom) }
        }
    }

    fun connectToChat() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    webSocketManager.connect(currentUserId, token)
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateMessageText(text: String) {
        val connectionState = _uiState.value.connectionState
        _uiState.update {
            it.copy(
                messageText = text,
                canSendMessage = text.isNotBlank() && connectionState == ConnectionState.CONNECTED
            )
        }
    }

    fun markAsRead() {
        val state = _uiState.value
        if (currentUserId != 0L && state.roomInfo.chatRoomID != 0L) {
            webSocketManager.markAsRead(
                roomId = state.roomInfo.chatRoomID.toString(),
                userId = currentUserId,
                senderId = state.roomInfo.partnerID
            )
        }
    }

    fun updateConnectionState(connectionState: ConnectionState) {
        _uiState.update { currentState ->
            currentState.copy(
                connectionState = connectionState,
                canSendMessage = currentState.messageText.isNotBlank() &&
                        connectionState == ConnectionState.CONNECTED
            )
        }
    }

    fun endChatRoom() {
        webSocketManager.disconnect()
        _tempMessages.value = emptyList()
        _realtimeMessages.value = emptyList()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun navigateToBack() {
        viewModelScope.launch {
            _naviEvent.emit(ChatNaviEvent.ToBack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        endChatRoom()
    }
}
