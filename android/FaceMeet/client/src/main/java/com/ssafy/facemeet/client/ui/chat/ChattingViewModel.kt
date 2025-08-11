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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // 현재 사용자 및 방 정보
    var currentUserId: Long = 0L
    var currentRoomId: Long = 0L

    // 상태 관리
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
    // 통합 메시지 리스트
    private val _unifiedMessages = MutableStateFlow<List<MessageItem>>(emptyList())

    val unifiedMessages: StateFlow<List<MessageItem>> = _unifiedMessages

    init {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }
    }

    // 채팅방 초기화
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

    // 초기 메시지 로드 (페이징)
    private fun loadInitialMessages(roomId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "초기 메시지 로드 시작: roomId=$roomId")

            val pagingFlow = getChattingMessagesCurrentUseCase.invoke(roomId).cachedIn(viewModelScope)
            _messageState.update { it.copy(pagedMessages = pagingFlow) }
            _unifiedMessages.value = emptyList()

            Log.d(TAG, "초기 메시지 로드 완료")
        }
    }

    // WebSocket 콜백 설정
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

    // 새 메시지 처리 로직
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
                updatePendingMessageToSent(newMessage)
                _myLastMessageId.value = generateMessageKey(newMessage.chatElement)
                markAsRead()
            } else {
                addNewMessage(newMessage, MessageStatus.RECEIVED)
                _unifiedMessages.update { current ->
                    current.map { it.copy(showReadStatus = false) }
                }
                markAsRead()
            }

            if (_uiState.value.scrollState.isAtBottom) {
                triggerScroll(ScrollEvent.ToBottom)
            }
        }
    }

    // 메시지 전송
    fun sendMessage() {
        val state = _uiState.value
        if (!state.canSendMessage || currentUserId == 0L) return

        val messageContent = state.messageText.trim()
        if (messageContent.isBlank()) return

        val sentMessage = createSentMessage(messageContent, state.roomInfo)
        addNewMessage(sentMessage.chatMessage, MessageStatus.SENT)

        _uiState.update {
            it.copy(
                messageText = "",
                canSendMessage = false,
                scrollState = it.scrollState.copy(isAtBottom = true)
            )
        }

        triggerScroll(ScrollEvent.ToBottom)

        viewModelScope.launch {
            try {
                webSocketManager.sendMessage(
                    content = messageContent,
                    roomId = state.roomInfo.chatRoomID,
                    senderId = currentUserId,
                    receiverId = state.roomInfo.partnerID
                )
                Log.d(TAG, "메시지 전송 성공")
            } catch (e: Exception) {
                Log.e(TAG, "메시지 전송 실패", e)
                _uiState.update { it.copy(error = "메시지 전송 실패: ${e.message}") }
            }
        }
    }

    // 새 메시지를 리스트에 추가
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
                showReadStatus = false
            )

            val updated = listOf(newItem) + current

            if (updated.size > REALTIME_MESSAGE_LIMIT) {
                updated.take(REALTIME_MESSAGE_LIMIT)
            } else {
                updated
            }
        }
    }

    // 메시지 읽음 처리
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

    // UI에서 읽음 상태 업데이트
    private fun updateReadStatusInUI() {
        _unifiedMessages.update { current ->
            var isLastSentMessageFound = false
            current.map { item ->
                if (!isLastSentMessageFound &&
                    item.chatMessage.chatElement.senderID == currentUserId &&
                    item.status == MessageStatus.SENT) {
                    isLastSentMessageFound = true
                    item.copy(showReadStatus = true)
                } else {
                    item.copy(showReadStatus = false)
                }
            }
        }
    }

    // PENDING 메시지를 SENT로 변경
    private fun updatePendingMessageToSent(sentMessage: ChatMessageItem) {
        _unifiedMessages.update { current ->
            current.map { item ->
                if (item.status == MessageStatus.PENDING &&
                    item.chatMessage.chatElement.content.trim() == sentMessage.chatElement.content.trim() &&
                    item.chatMessage.chatElement.senderID == sentMessage.chatElement.senderID) {
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

    // 메시지를 실패 상태로 변경
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

    // 메시지 키 생성
    private fun generateMessageKey(chatElement: ChatElement): String {
        return "${chatElement.senderID}_${chatElement.roomID}_${chatElement.content}_${chatElement.createdAt}"
    }

    // 로컬 ID 생성
    private fun generateLocalId(content: String): String {
        return "local_${currentUserId}_${System.currentTimeMillis()}_${content.hashCode()}"
    }

    // 전송할 메시지 생성
    private fun createSentMessage(content: String, roomInfo: ChatRoom): MessageItem {
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
            status = MessageStatus.SENT
        )
    }

    // 스크롤 상태 변경 처리
    fun onScrollStateChanged(isScrolling: Boolean, firstVisibleItemIndex: Int, totalItemCount: Int) {
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

    // 초기 로드 완료 처리
    fun onInitialLoadComplete() {
        triggerScroll(ScrollEvent.ToBottom)
    }

    // 키보드 표시시 처리
    fun onKeyboardShown() {
        if (_uiState.value.scrollState.isAtBottom) {
            triggerScroll(ScrollEvent.WithKeyboard)
        }
    }

    // 수동으로 하단 스크롤
    fun scrollToBottomManually() {
        _uiState.update {
            it.copy(scrollState = it.scrollState.copy(isAtBottom = true))
        }
        triggerScroll(ScrollEvent.ToBottom)
    }

    // 스크롤 이벤트 트리거
    private fun triggerScroll(event: ScrollEvent) {
        viewModelScope.launch {
            _scrollEvent.emit(event)
        }
    }

    // 공지사항 토글
    fun toggleNotice() {
        _uiState.update { it.copy(isNoticeOpen = !it.isNoticeOpen) }
    }

    // 채팅방 정보 로드
    suspend fun loadChatRoomInfo(roomId: Long) {
        getChattingMessagesLastUseCase.invoke(roomId, 30).onSuccess { chattingAll ->
            _uiState.update { it.copy(roomInfo = chattingAll.chatRoom) }
        }
    }

    // 채팅 연결
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

    // 메시지 텍스트 업데이트
    fun updateMessageText(text: String) {
        val connectionState = _uiState.value.connectionState
        _uiState.update {
            it.copy(
                messageText = text,
                canSendMessage = text.isNotBlank() && connectionState == ConnectionState.CONNECTED
            )
        }
    }

    // 연결 상태 업데이트
    fun updateConnectionState(connectionState: ConnectionState) {
        _uiState.update { currentState ->
            currentState.copy(
                connectionState = connectionState,
                canSendMessage = currentState.messageText.isNotBlank() &&
                        connectionState == ConnectionState.CONNECTED
            )
        }
    }

    // 채팅방 종료
    fun endChatRoom() {
        webSocketManager.disconnect()
        _unifiedMessages.value = emptyList()
    }

    // 에러 초기화
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // 뒤로 가기 네비게이션
    fun navigateToBack() {
        viewModelScope.launch {
            _naviEvent.emit(ChatNaviEvent.ToBack)
        }
    }

    // 프로필 네비게이션
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