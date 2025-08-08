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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChattingViewModel @Inject constructor(
    private val webSocketManager: ChatWebSocketManager,
    private val tokenManager: TokenManager,
    private val getChattingMessagesLastUseCase: GetChattingMessagesLastUseCase,
    private val getChattingMessagesCurrentUseCase: GetChattingMessagesCurrentUseCase
) : ViewModel() {

    var currentUserId: Long = 0L
    var currentRoomId: Long = 0L

    private val _messageState = MutableStateFlow(MessageState())
    val messageState: StateFlow<MessageState> = _messageState

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
    val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

    private val _scrollEvent = MutableSharedFlow<ScrollEvent>()
    val scrollEvent: SharedFlow<ScrollEvent> = _scrollEvent.asSharedFlow()

    val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

    // 단일 메시지 상태로 통합 - 메시지 상태를 enum으로 관리
    enum class MessageStatus { PENDING, SENT, FAILED, RECEIVED }

    data class MessageItem(
        val chatMessage: ChatMessageItem,
        val status: MessageStatus = MessageStatus.RECEIVED,
        val localId: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    )

    // 통합된 메시지 리스트 - 하나의 StateFlow로 관리
    private val _unifiedMessages = MutableStateFlow<List<MessageItem>>(emptyList())

    val liveMessages: StateFlow<List<ChatMessageItem>> = _unifiedMessages
        .map { items ->
            items.map { it.chatMessage } // 정렬 제거 - 이미 올바른 순서로 추가됨
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // UI에서 메시지 상태를 확인할 수 있도록 unifiedMessages도 노출
    val unifiedMessages: StateFlow<List<MessageItem>> = _unifiedMessages

    init {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }
    }

    fun initializeChat(roomId: Long) {
        webSocketManager.disconnect()
        viewModelScope.launch {
            currentRoomId = roomId
            _uiState.update { it.copy(isLoading = true) }
            loadChatRoomInfo(roomId)
            loadInitialMessages(roomId)
            setupWebSocketCallbacks()
            connectToChat()
        }
    }

    private fun loadInitialMessages(roomId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "초기 메시지 로드 시작: roomId=$roomId")

            val pagingFlow = getChattingMessagesCurrentUseCase.invoke(roomId)
                .cachedIn(viewModelScope)

            _messageState.update {
                it.copy(pagedMessages = pagingFlow)
            }

            // 통합 메시지 리스트 초기화
            _unifiedMessages.value = emptyList()

            Log.d(TAG, "초기 메시지 로드 완료")
        }
    }

    private fun setupWebSocketCallbacks() {
        webSocketManager.setOnNewMessageCallback { newMessage ->
            Log.d(TAG, "🔴 WebSocket 메시지 수신: ${newMessage.chatElement.content}")
            handleNewMessage(newMessage)
        }
    }

    private fun handleNewMessage(newMessage: ChatMessageItem) {
        viewModelScope.launch {
            if (newMessage.chatElement.roomID != currentRoomId) return@launch

            // 중복 체크를 더 안정적으로
            val messageKey = generateMessageKey(newMessage.chatElement)
            val isDuplicate = _unifiedMessages.value.any {
                generateMessageKey(it.chatMessage.chatElement) == messageKey
            }

            if (isDuplicate) {
                Log.d(TAG, "중복 메시지 무시: $messageKey")
                return@launch
            }

            Log.d(TAG, "📩 새 메시지 처리: ${newMessage.chatElement.content}")

            // 내가 보낸 메시지인 경우 PENDING 상태의 메시지를 SENT로 변경
            if (newMessage.chatElement.senderID == currentUserId) {
                updatePendingMessageToSent(newMessage)
            } else {
                // 상대방 메시지는 바로 추가
                addNewMessage(newMessage, MessageStatus.RECEIVED)
            }

            // 자동 스크롤
            if (_uiState.value.scrollState.isAtBottom) {
                triggerScroll(ScrollEvent.ToBottom)
            }
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        if (!state.canSendMessage || currentUserId == 0L) return

        val messageContent = state.messageText.trim()
        if (messageContent.isBlank()) return

        val localId = generateLocalId(messageContent)

        // 즉시 PENDING 상태로 메시지 추가
        val pendingMessage = createPendingMessage(messageContent, state.roomInfo, localId)
        addNewMessage(pendingMessage.chatMessage, MessageStatus.PENDING, localId)

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

        // 웹소켓으로 전송
        viewModelScope.launch {
            try {
                webSocketManager.sendMessage(
                    content = messageContent,
                    roomId = state.roomInfo.chatRoomID,
                    senderId = currentUserId,
                    receiverId = state.roomInfo.partnerID,
                    tempId = System.currentTimeMillis()
                )

                // 타임아웃 처리
                delay(TEMP_MESSAGE_TIMEOUT)
                markMessageAsFailed(localId)

            } catch (e: Exception) {
                Log.e(TAG, "메시지 전송 실패", e)
                markMessageAsFailed(localId)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // 통합된 메시지 관리 메서드들
    private fun addNewMessage(
        message: ChatMessageItem,
        status: MessageStatus,
        localId: String? = null
    ) {
        _unifiedMessages.update { current ->
            val newItem = MessageItem(
                chatMessage = message,
                status = status,
                localId = localId
            )

            // reverseLayout이므로 최신 메시지를 맨 앞에 추가
            val updated = listOf(newItem) + current

            // 메시지 수 제한
            if (updated.size > REALTIME_MESSAGE_LIMIT) {
                updated.take(REALTIME_MESSAGE_LIMIT) // 앞에서부터 제한
            } else {
                updated
            }
        }
    }

    private fun updatePendingMessageToSent(sentMessage: ChatMessageItem) {
        _unifiedMessages.update { current ->
            current.map { item ->
                if (item.status == MessageStatus.PENDING &&
                    item.chatMessage.chatElement.content.trim() == sentMessage.chatElement.content.trim() &&
                    item.chatMessage.chatElement.senderID == sentMessage.chatElement.senderID) {
                    // PENDING 메시지를 실제 메시지로 교체
                    item.copy(
                        chatMessage = sentMessage,
                        status = MessageStatus.SENT
                    )
                } else {
                    item
                }
            }
        }
    }

    private fun markMessageAsFailed(localId: String) {
        _unifiedMessages.update { current ->
            current.map { item ->
                if (item.localId == localId && item.status == MessageStatus.PENDING) {
                    item.copy(status = MessageStatus.FAILED)
                } else {
                    item
                }
            }
        }
    }

    // 유틸리티 메서드들
    private fun generateMessageKey(chatElement: ChatElement): String {
        return "${chatElement.senderID}_${chatElement.roomID}_${chatElement.content}_${chatElement.createdAt}"
    }

    private fun generateLocalId(content: String): String {
        return "local_${currentUserId}_${System.currentTimeMillis()}_${content.hashCode()}"
    }

    private fun createPendingMessage(
        content: String,
        roomInfo: ChatRoom,
        localId: String
    ): MessageItem {
        val chatElement = ChatElement(
            content = content,
            senderID = currentUserId,
            receiverID = roomInfo.partnerID,
            roomID = roomInfo.chatRoomID,
            createdAt = LocalDateTime.now().toString(),
            isRead = false,
            readAt = ""
        )

        val messageItem = ChatMessageItem(
            chatElement = chatElement,
            messageType = MessageType.TEXT
        )

        return MessageItem(
            chatMessage = messageItem,
            status = MessageStatus.PENDING,
            localId = localId
        )
    }

    // 스크롤 관련 메서드들 (기존과 동일)
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

    // 나머지 메서드들은 기존과 동일하게 유지...
    fun toggleNotice() {
        _uiState.update { it.copy(isNoticeOpen = !it.isNoticeOpen) }
    }

    suspend fun loadChatRoomInfo(roomId: Long) {
        getChattingMessagesLastUseCase.invoke(roomId, 30).onSuccess { chattingAll ->
            _uiState.update { it.copy(roomInfo = chattingAll.chatRoom) }
        }
    }

    fun connectToChat() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    webSocketManager.connect(currentUserId, token, currentRoomId)
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
        _unifiedMessages.value = emptyList()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun navigateToBack() {
        viewModelScope.launch {
            _naviEvent.emit(ChatNaviEvent.ToBack)
        }
    }

    fun navigateToProfile() {
        viewModelScope.launch {
            _naviEvent.emit(ChatNaviEvent.ToProfile)
        }
    }

    override fun onCleared() {
        super.onCleared()
        endChatRoom()
    }

    companion object {
        private const val TAG = "ChattingViewModel"
        private const val TEMP_MESSAGE_TIMEOUT = 10000L
        private const val REALTIME_MESSAGE_LIMIT = 50
    }
}