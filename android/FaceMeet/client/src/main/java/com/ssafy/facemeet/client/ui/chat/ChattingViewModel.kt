package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ssafy.facemeet.client.ui.chat.model.ChatNaviEvent
import com.ssafy.facemeet.client.ui.chat.model.ChatUiState
import com.ssafy.facemeet.client.ui.chat.model.MessageItem
import com.ssafy.facemeet.client.ui.chat.model.MessageState
import com.ssafy.facemeet.client.ui.chat.model.MessageStatus
import com.ssafy.facemeet.client.ui.chat.model.ScrollEvent
import com.ssafy.facemeet.client.ui.chatlist.ChatListUiState
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.domain.model.ChatRoom
import com.ssafy.facemeet.core.domain.usecase.GetChattingListUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesCurrentUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesLastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val getChattingMessagesCurrentUseCase: GetChattingMessagesCurrentUseCase,
    private val getChattingListUseCase: GetChattingListUseCase,
) : ViewModel() {

    internal var currentUserId: Long = 0L
    private var currentRoomId: Long = 0L
    private var currentPartnerId : Long = 0L

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _listUiState = MutableStateFlow(ChatListUiState())
    val listUiState = _listUiState.asStateFlow()

    private val _messageState = MutableStateFlow(MessageState())
    val messageState: StateFlow<MessageState> = _messageState.asStateFlow()

    private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
    val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

    private val _scrollEvent = MutableSharedFlow<ScrollEvent>()
    val scrollEvent: SharedFlow<ScrollEvent> = _scrollEvent.asSharedFlow()

    val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

    private val _unifiedMessages = MutableStateFlow<List<MessageItem>>(emptyList())
    val unifiedMessages: StateFlow<List<MessageItem>> = _unifiedMessages.asStateFlow()

    private val _isScreenActive = MutableStateFlow(false)
    val isScreenActive: StateFlow<Boolean> = _isScreenActive.asStateFlow()

    // 💡 모든 메시지 (수신 및 발신)를 위한 단일 채널
    private val messageUpdateChannel = Channel<ChatMessageItem>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }

        // 💡 모든 메시지를 모아서 UI에 업데이트하는 단일 `Flow`
        viewModelScope.launch {
            messageUpdateChannel
                .receiveAsFlow()
                .debounce(50) // 💡 200ms 동안 메시지 수신이 없으면 한 번에 업데이트
                .collect { batchedMessage ->
                    _unifiedMessages.update { current ->
                        // 💡 이 로직이 모든 메시지를 처리합니다.
                        val messageItem = MessageItem(
                            chatMessage = batchedMessage,
                            status = if (batchedMessage.chatElement.senderID == currentUserId) MessageStatus.SENT else MessageStatus.RECEIVED,
                            localId = generateLocalId(batchedMessage.chatElement.content),
                            showReadStatus = false
                        )
                        listOf(messageItem) + current.take(REALTIME_MESSAGE_LIMIT)
                    }
                }
        }
    }

    fun onScreenResume() {
        _isScreenActive.value = true
        if (!uiState.value.roomInfo.deleted && !uiState.value.roomInfo.blocked) {
            Log.d("ChatDebug", "상대방 앱: '읽음' 알림 전송")
            markAsRead()
        }
    }

    fun onScreenPause() {
        _isScreenActive.value = false
    }

    fun initializeChat(roomId: Long ) {
        webSocketManager.disconnect()
        viewModelScope.launch {
            loadChatRoomInfo(roomId)
            currentRoomId = roomId
            currentPartnerId = uiState.value.roomInfo.partnerID
            _uiState.update { it.copy(isLoading = true) }
            loadInitialMessages(roomId)
            setupWebSocketCallbacks()
            connectToChat()
            Log.d(TAG, "STOMP connection successful. Marking as read.")
        }
    }

    private fun loadInitialMessages(roomId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load initial messages: roomId=$roomId")
            val pagingFlow = getChattingMessagesCurrentUseCase.invoke(roomId).cachedIn(viewModelScope)
            _messageState.update { it.copy(pagedMessages = pagingFlow) }
            _unifiedMessages.value = emptyList()
            Log.d(TAG, "Initial message load complete")
        }
    }

    private fun setupWebSocketCallbacks() {
        webSocketManager.setOnNewMessageCallback(::handleNewMessage)
        webSocketManager.onReadNotification = {
            Log.d("ChatDebug", "내 앱: 서버로부터 '읽음' 알림 받음!")
            viewModelScope.launch {
                updateReadStatusInUI()
            }
        }
        webSocketManager.setOnStompConnectedCallback {
            if (!uiState.value.roomInfo.deleted && !uiState.value.roomInfo.blocked) {
                markAsRead()
            }
        }
        webSocketManager.onNewMessageForList={
            Log.d(TAG, "onNewMessageForList")
        }
        webSocketManager.onNewMessageLeaved ={
            Log.d(TAG, "onNewMessageLeaved")
            addNewMessage(it, MessageStatus.RECEIVED)
            try {
                webSocketManager.sendMessage(
                    content = it.chatElement.content,
                    roomId = it.chatElement.roomID?.toLong() ?: -1L,
                    senderId = -1L,
                    receiverId =  currentUserId
                )
                Log.d(TAG, "Message sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Message sending failed", e)
                _uiState.update { it.copy(error = "Message sending failed: ${e.message}") }
            }
            _uiState.update { currentState ->
                currentState.copy(
                    roomInfo = currentState.roomInfo.copy(blocked = true)
                )
            }
        }
    }

    // 💡 handleNewMessage 함수: 메시지를 Channel에 보내는 역할만 담당
    private fun handleNewMessage(newMessage: ChatMessageItem) {
        viewModelScope.launch {
            if (newMessage.chatElement.roomID != currentRoomId || newMessage.chatElement.senderID == currentUserId) {
                Log.w(TAG, "Ignoring message for a different room or from self.")
                return@launch
            }
            Log.d(TAG, "📩 새로운 메시지 수신: ${newMessage.chatElement.content}")
            messageUpdateChannel.send(newMessage)
            if (newMessage.chatElement.senderID != currentUserId && !uiState.value.scrollState.isAtBottom) {
                _uiState.update { it.copy(newMessageContent = newMessage.chatElement.content) }
            }
            if (_isScreenActive.value) {
                Log.d(TAG, "🟢 handleNewMessage: 화면 활성화 상태, markAsRead() 호출 시작")
                markAsRead()
            }
            if (_uiState.value.scrollState.isAtBottom) {
                triggerScroll(ScrollEvent.ToBottom)
            }
        }
    }

    // 💡 sendMessage 함수: 메시지를 Channel에 보내는 역할만 담당
    fun sendMessage() {
        val state = _uiState.value
        if (!state.canSendMessage || currentUserId == 0L) return

        val sentMessage = createSentMessage(state.messageText.trim(), state.roomInfo)

        viewModelScope.launch {
            messageUpdateChannel.send(sentMessage.chatMessage)
        }

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
                    content = sentMessage.chatMessage.chatElement.content,
                    roomId = state.roomInfo.chatRoomID,
                    senderId = currentUserId,
                    receiverId = state.roomInfo.partnerID
                )
                Log.d(TAG, "Message sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Message sending failed", e)
                _uiState.update { it.copy(error = "Message sending failed: ${e.message}") }
            }
        }
    }

    private fun addNewMessage(message: ChatMessageItem, status: MessageStatus) {
        _unifiedMessages.update { current ->
            val newItem = MessageItem(
                chatMessage = message,
                status = status,
                localId = generateLocalId(message.chatElement.content),
                showReadStatus = false
            )
            val updated = listOf(newItem) + current
            updated.take(REALTIME_MESSAGE_LIMIT)
        }
    }

    private fun markAsRead() {
        val currentPartnerId = uiState.value.roomInfo.partnerID
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔍 단계 1 - '읽음' 처리 요청 전송: roomId=$currentRoomId, senderId=$currentUserId, partnerId=$currentPartnerId")
                webSocketManager.markAsRead(currentRoomId, currentUserId, currentPartnerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark as read", e)
            }
        }
    }

    private fun updateReadStatusInUI() {
        Log.d("WebSocket-ReadStatus", "⚙️ updateReadStatusInUI() 함수 호출 시작")
        if (uiState.value.roomInfo.deleted || uiState.value.roomInfo.blocked) {
            return
        }
        _unifiedMessages.update { current ->
            val updatedList = current.map { item ->
                if (item.chatMessage.chatElement.senderID == currentUserId && item.status == MessageStatus.SENT && !item.showReadStatus) {
                    Log.d("WebSocket-ReadStatus", "✅ 메시지 상태 업데이트: 메시지 [${item.chatMessage.chatElement.content}]의 showReadStatus를 true로 변경")
                    item.copy(showReadStatus = true)
                } else {
                    item
                }
            }
            Log.d("UpdateDebug", "최종 업데이트 리스트 상태: ${updatedList.firstOrNull()?.showReadStatus}")
            updatedList
        }
    }

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
        val messageItem = ChatMessageItem(chatElement = chatElement, messageType = MessageType.TEXT)
        return MessageItem(chatMessage = messageItem, status = MessageStatus.SENT)
    }

    private fun generateMessageKey(chatElement: ChatElement): String {
        return "${chatElement.senderID}_${chatElement.roomID}_${chatElement.content}_${chatElement.createdAt}"
    }

    private fun generateLocalId(content: String): String {
        val safeContent = content ?: "empty"
        return "local_${currentUserId}_${System.currentTimeMillis()}_${safeContent.hashCode()}"
    }

    fun onScrollStateChanged(isScrolling: Boolean, firstVisibleItemIndex: Int) {
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
        _uiState.update { currentState ->
            currentState.copy(
                scrollState = currentState.scrollState.copy(isAtBottom = true),
                newMessageContent = null
            )
        }
        triggerScroll(ScrollEvent.ToBottom)
    }

    private fun triggerScroll(event: ScrollEvent) {
        viewModelScope.launch { _scrollEvent.emit(event) }
    }

    fun toggleNotice() {
        _uiState.update { it.copy(isNoticeOpen = !it.isNoticeOpen) }
    }

    private suspend fun loadChatRoomInfo(roomId: Long) {
        getChattingMessagesLastUseCase.invoke(roomId, 30)
            .onSuccess { chattingAll ->
                Log.d(TAG, "Loaded chat room info: ${chattingAll.chatRoom}")
                _uiState.update { it.copy(roomInfo = chattingAll.chatRoom) }
            }
            .onFailure { e ->
                Log.e(TAG, "Failed to load chat room info", e)
                _uiState.update { it.copy(error = "Failed to load chat room info: ${e.message}") }
            }
    }

    fun connectToChat() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    webSocketManager.connect(currentUserId, token, currentRoomId,currentPartnerId)
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateMessageText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                messageText = text,
                canSendMessage = text.isNotBlank() && currentState.connectionState == ConnectionState.CONNECTED
            )
        }
    }

    fun updateConnectionState(connectionState: ConnectionState) {
        _uiState.update { currentState ->
            currentState.copy(
                connectionState = connectionState,
                canSendMessage = currentState.messageText.isNotBlank() && connectionState == ConnectionState.CONNECTED
            )
        }
    }

    fun endChatRoom() {
        _unifiedMessages.value = emptyList()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun navigateToBack() {
        viewModelScope.launch { _naviEvent.emit(ChatNaviEvent.ToBack) }
    }

    fun navigateToProfile() {
        viewModelScope.launch { _naviEvent.emit(ChatNaviEvent.ToProfile) }
    }

    override fun onCleared() {
        super.onCleared()
        endChatRoom()
    }

    companion object {
        private const val TAG = "ChattingViewModel"
        private const val REALTIME_MESSAGE_LIMIT = 50
    }
}