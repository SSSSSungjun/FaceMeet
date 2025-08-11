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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

        private val _myLastMessageId = MutableStateFlow<String?>(null)
        val myLastMessageId: StateFlow<String?> = _myLastMessageId.asStateFlow()

        private val _showReadStatus = MutableStateFlow(false)
        val showReadStatus: StateFlow<Boolean> = _showReadStatus.asStateFlow()

        private val _messageState = MutableStateFlow(MessageState())
        val messageState: StateFlow<MessageState> = _messageState

        private val _uiState = MutableStateFlow(ChatUiState())
        val uiState: StateFlow<ChatUiState> = _uiState

        private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
        val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

        private val _scrollEvent = MutableSharedFlow<ScrollEvent>()
        val scrollEvent: SharedFlow<ScrollEvent> = _scrollEvent.asSharedFlow()

        val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

        enum class MessageStatus { PENDING, SENT, FAILED, RECEIVED }

        data class MessageItem(
            val chatMessage: ChatMessageItem,
            val status: MessageStatus = MessageStatus.RECEIVED,
            val localId: String? = null,
            val timestamp: Long = System.currentTimeMillis(),
            val showReadStatus: Boolean = false
        )

        // 통합된 메시지 리스트 - 하나의 StateFlow로 관리
        private val _unifiedMessages = MutableStateFlow<List<MessageItem>>(emptyList())

        val liveMessages: StateFlow<List<ChatMessageItem>> = _unifiedMessages
            .map { items ->
                items.map { it.chatMessage }
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

            webSocketManager.onReadNotification = {
                Log.d(TAG, "📖 읽음 알림 수신 - 상대방이 내 메시지를 읽음")
                viewModelScope.launch {
                    updateReadStatusInUI()
                }
            }

            webSocketManager.setOnStompConnectedCallback {
                Log.d(TAG, "STOMP 연결 완료. 마크업 처리 시작")
                markAsRead()
            }
        }

        private fun handleNewMessage(newMessage: ChatMessageItem) {
            viewModelScope.launch {
                if (newMessage.chatElement.roomID != currentRoomId) return@launch
                val existingItem = _unifiedMessages.value.find {
                    it.status == MessageStatus.PENDING &&
                            it.chatMessage.chatElement.content.trim() == newMessage.chatElement.content.trim() &&
                            it.chatMessage.chatElement.senderID == currentUserId
                }

                if (existingItem != null) {
                    updatePendingMessageToSent(newMessage)
                    Log.d(TAG, "✅ PENDING 메시지를 SENT로 업데이트: ${newMessage.chatElement.content}")
                    return@launch
                }

                val messageKey = generateMessageKey(newMessage.chatElement)
                val isDuplicate = _unifiedMessages.value.any {
                    generateMessageKey(it.chatMessage.chatElement) == messageKey
                }

                if (isDuplicate) {
                    Log.d(TAG, "중복 메시지 무시: $messageKey")
                    return@launch
                }

                Log.d(TAG, "📩 새 메시지 처리: ${newMessage.chatElement.content}")

                if (newMessage.chatElement.senderID == currentUserId) {
                    // 내가 보낸 메시지: PENDING를 SENT로 변경
                    updatePendingMessageToSent(newMessage)
                    _myLastMessageId.value = generateMessageKey(newMessage.chatElement)

                    // ✅ 원래대로: 내 메시지 전송 후 읽음 처리
                    markAsRead()

                } else {
                    // 상대방 메시지: 바로 추가하고 읽음 표시 제거
                    addNewMessage(newMessage, MessageStatus.RECEIVED)

                    // 새 메시지 수신 시 기존 읽음 표시 제거
                    _unifiedMessages.update { current ->
                        current.map { it.copy(showReadStatus = false) }
                    }

                    // ✅ 원래대로: 상대방 메시지 받으면 읽음 처리
                    markAsRead()
                }

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

            // 💡 localId를 제거하고, 전송할 메시지를 바로 SENT 상태로 생성
            val sentMessage = createSentMessage(messageContent, state.roomInfo)
            addNewMessage(sentMessage.chatMessage, MessageStatus.SENT)

            // 기존 UI 상태 업데이트 코드...
            _uiState.update {
                it.copy(
                    messageText = "",
                    canSendMessage = false,
                    scrollState = it.scrollState.copy(isAtBottom = true)
                )
            }

            triggerScroll(ScrollEvent.ToBottom)

            // WebSocket 전송 코드...
            viewModelScope.launch {
                try {
                    webSocketManager.sendMessage(
                        content = messageContent,
                        roomId = state.roomInfo.chatRoomID,
                        senderId = currentUserId,
                        receiverId = state.roomInfo.partnerID
                    )
                    Log.d(TAG, "메시지 전송 성공 (옵티미스틱 업데이트)")
                } catch (e: Exception) {
                    Log.e(TAG, "메시지 전송 실패 (오류를 UI에 표시하는 로직이 필요)", e)
                    _uiState.update { it.copy(error = "메시지 전송 실패: ${e.message}") }
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
                    localId = localId,
                    showReadStatus = false // 새 메시지는 읽음 표시 없음
                )

                val updated = listOf(newItem) + current

                if (updated.size > REALTIME_MESSAGE_LIMIT) {
                    updated.take(REALTIME_MESSAGE_LIMIT)
                } else {
                    updated
                }
            }
        }

        private fun markAsRead() {
            val partnerId = uiState.value.roomInfo.partnerID
            viewModelScope.launch {
                try {
                    Log.d(TAG, "✅ 읽음 처리 실행")
                    webSocketManager.markAsRead(currentRoomId, currentUserId, partnerId)
                } catch (e: Exception) {
                    Log.e(TAG, "읽음 처리 실패", e)
                }
            }
        }

        private fun updateReadStatusInUI() {
            _unifiedMessages.update { current ->
                var isLastSentMessageFound = false
                current.map { item ->
                    if (!isLastSentMessageFound &&
                        item.chatMessage.chatElement.senderID == currentUserId &&
                        item.status == MessageStatus.SENT) {
                        isLastSentMessageFound = true
                        item.copy(showReadStatus = true) // 가장 최근에 보낸 메시지에만 표시
                    } else {
                        item.copy(showReadStatus = false)
                    }
                }
            }
        }

        private fun     updatePendingMessageToSent(sentMessage: ChatMessageItem) {
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

        private fun createSentMessage(
            content: String,
            roomInfo: ChatRoom
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
                status = MessageStatus.SENT // 바로 SENT 상태로 반환
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