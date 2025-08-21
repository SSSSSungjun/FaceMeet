package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ssafy.facemeet.client.ui.chat.model.ChatNaviEvent
import com.ssafy.facemeet.client.ui.chat.model.ChatUiState
import com.ssafy.facemeet.client.ui.chat.model.MessageItem
import com.ssafy.facemeet.client.ui.chat.model.MessageState
import com.ssafy.facemeet.client.ui.chat.model.MessageStatus
import com.ssafy.facemeet.client.ui.chat.model.ScrollEvent
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.domain.model.ChatRoom
import com.ssafy.facemeet.core.domain.usecase.ConnectChatWebSocketUseCase
import com.ssafy.facemeet.core.domain.usecase.DisconnectChatWebSocketUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesCurrentUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesLastUseCase
import com.ssafy.facemeet.core.domain.usecase.MarkMessageAsReadUseCase
import com.ssafy.facemeet.core.domain.usecase.ObserveChatEventsUseCase
import com.ssafy.facemeet.core.domain.usecase.SendChatMessageUseCase
import com.ssafy.facemeet.core.util.AppStateManager
import com.ssafy.facemeet.core.util.messaging.ChatMessageItem
import com.ssafy.facemeet.core.util.messaging.ConnectionState
import com.ssafy.facemeet.core.util.messaging.MessageType
import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChattingViewModel @Inject constructor(
    private val connectWebSocketUseCase: ConnectChatWebSocketUseCase,
    private val disconnectWebSocketUseCase: DisconnectChatWebSocketUseCase,
    private val sendMessageUseCase: SendChatMessageUseCase,
    private val markAsReadUseCase: MarkMessageAsReadUseCase,
    private val observeChatEventsUseCase: ObserveChatEventsUseCase,

    private val tokenManager: TokenManager,
    private val getChattingMessagesLastUseCase: GetChattingMessagesLastUseCase,
    private val getChattingMessagesCurrentUseCase: GetChattingMessagesCurrentUseCase,
) : ViewModel() {

    // ===== 상태 변수 =====
    internal var currentUserId: Long = 0L
    private var currentRoomId: Long = 0L
    private var currentPartnerId: Long = 0L

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageState = MutableStateFlow(MessageState())
    val messageState: StateFlow<MessageState> = _messageState.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _unifiedMessages = MutableStateFlow<List<MessageItem>>(emptyList())
    val unifiedMessages: StateFlow<List<MessageItem>> = _unifiedMessages.asStateFlow()

    private val _isScreenActive = MutableStateFlow(false)
    val isScreenActive: StateFlow<Boolean> = _isScreenActive.asStateFlow()

    // 이벤트 스트림
    private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
    val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

    private val _scrollEvent = MutableSharedFlow<ScrollEvent>()
    val scrollEvent: SharedFlow<ScrollEvent> = _scrollEvent.asSharedFlow()

    // 내부 처리용
    private val incomingMessageChannel = Channel<ChatMessageItem>(Channel.UNLIMITED)
    private val localIdCounter = AtomicLong(0)

    // init & lifecycle
    init {
        AppStateManager.setCurrentScreen("ChattingScreen", currentRoomId)
        initializeViewModel()
    }

    private fun initializeViewModel() {
        observeWebSocketEvents()
        setupIncomingMessageProcessor()

        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }
    }

    fun initializeChat(roomId: Long) {
        disconnectWebSocketUseCase()

        viewModelScope.launch {
            loadChatRoomInfo(roomId)
            currentRoomId = roomId
            currentPartnerId = uiState.value.roomInfo.partnerID
            _uiState.update { it.copy(isLoading = true) }
            loadInitialMessages(roomId)
            connectToChat()
        }
    }

    fun onScreenResume() {
        _isScreenActive.value = true
        viewModelScope.launch {
            loadInitialMessages(currentRoomId)
        }
    }

    fun onScreenPause() {
        _isScreenActive.value = false
    }

    override fun onCleared() {
        super.onCleared()
        AppStateManager.clearCurrentScreen()
        disconnectWebSocketUseCase()
        endChatRoom()
    }

    // connect
    fun connectToChat() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    connectWebSocketUseCase(currentUserId, token)
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observeWebSocketEvents() {
        observeChatEventsUseCase()
            .onEach { event ->
                Log.d(TAG, "initializeViewModel:$event ")
                when (event) {
                    is WebSocketEvent.ConnectionStateChanged -> {
                        handleConnectionStateChanged(event.state)
                    }
                    is WebSocketEvent.StompConnected -> {
                        handleStompConnected()
                    }
                    is WebSocketEvent.NewMessage -> {
                        handleNewMessage(event.messageItem)
                    }
                    is WebSocketEvent.UserLeft -> {
                        handleUserLeft(event.messageItem)
                    }
                    is WebSocketEvent.ReadNotification -> {
                        handleReadNotification()
                    }
                    is WebSocketEvent.MessageSentConfirmation -> {
                        handleMessageSentConfirmation(event.content, event.senderId)
                    }
                    is WebSocketEvent.WebSocketError -> {
                        handleWebSocketError(event.throwable)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleConnectionStateChanged(state: ConnectionState) {
        _connectionState.value = state
        updateConnectionState(state)
    }

    private fun handleStompConnected() {
        if (!uiState.value.roomInfo.deleted && !uiState.value.roomInfo.blocked) {
            markAsRead()
        }
    }

    private fun handleWebSocketError(throwable: Throwable) {
        _uiState.update { it.copy(error = "Connection error: ${throwable.message}") }
    }

    // 전송
    fun sendMessage() {
        val state = _uiState.value
        if (currentUserId == 0L || state.messageText.trim().isBlank()) return

        val messageContent = state.messageText.trim()

        // UI 즉시 업데이트
        _uiState.update { it.copy(messageText = "") }

        // 메시지 생성 및 PENDING 상태로 추가
        val sentMessage = createSentMessage(messageContent, state.roomInfo)
        addNewMessage(sentMessage.chatMessage, MessageStatus.PENDING)
        triggerScroll(ScrollEvent.ToBottom)

        // 실제 전송
        sendMessageUseCase(
            content = messageContent,
            roomId = state.roomInfo.chatRoomID,
            senderId = currentUserId,
            receiverId = state.roomInfo.partnerID
        )
    }

    private fun handleMessageSentConfirmation(content: String, senderId: Long) {
        updateMessageStatusFromPendingToSent(content, senderId)
    }

    private fun updateMessageStatusFromPendingToSent(content: String, senderId: Long) {
        _unifiedMessages.update { current ->
            current.map { item ->
                if (item.chatMessage.chatElement.content == content &&
                    item.chatMessage.chatElement.senderID == senderId &&
                    item.status == MessageStatus.PENDING
                ) {
                    item.copy(status = MessageStatus.SENT)
                } else {
                    item
                }
            }
        }
    }

    // 수신
    private fun setupIncomingMessageProcessor() {
        viewModelScope.launch {
            incomingMessageChannel.receiveAsFlow().collect { batchedMessage ->
                _unifiedMessages.update { current ->
                    val existingMessageIndex = findExistingMessageIndex(current, batchedMessage)
                    if (existingMessageIndex != -1) {
                        updateExistingMessage(current, existingMessageIndex, batchedMessage)
                    } else {
                        addNewMessageToList(current, batchedMessage)
                    }
                }
                if (uiState.value.scrollState.isAtBottom) {
                    triggerScroll(ScrollEvent.ToBottom)
                }
            }
        }
    }

    private fun handleNewMessage(newMessage: ChatMessageItem) {
        viewModelScope.launch {
            if (newMessage.chatElement.roomID != currentRoomId) {
                return@launch
            }

            if (newMessage.chatElement.senderID == currentUserId) {
                return@launch
            }

            incomingMessageChannel.send(newMessage)

            if (!uiState.value.scrollState.isAtBottom) {
                _uiState.update { it.copy(newMessageContent = newMessage.chatElement.content) }
            }

            if (_isScreenActive.value) {
                markAsRead()
            }
        }
    }

    private fun processIncomingMessage(batchedMessage: ChatMessageItem) {
        _unifiedMessages.update { current ->
            val existingMessageIndex = findExistingMessageIndex(current, batchedMessage)

            if (existingMessageIndex != -1) {
                updateExistingMessage(current, existingMessageIndex, batchedMessage)
            } else {
                addNewMessageToList(current, batchedMessage)
            }
        }
    }

    private fun handleUserLeft(messageItem: ChatMessageItem) {
        addNewMessage(messageItem, MessageStatus.RECEIVED)

        sendMessageUseCase(
            content = messageItem.chatElement.content,
            roomId = messageItem.chatElement.roomID?.toLong() ?: -1L,
            senderId = -1L,
            receiverId = currentUserId
        )

        _uiState.update { currentState ->
            currentState.copy(
                roomInfo = currentState.roomInfo.copy(blocked = true)
            )
        }
    }

    // 읽음 로직
    private fun markAsRead() {
        val currentPartnerId = uiState.value.roomInfo.partnerID
        markAsReadUseCase(currentRoomId, currentUserId, currentPartnerId)
    }

    private fun handleReadNotification() {
        updateReadStatusInUI()
    }

    private fun updateReadStatusInUI() {
        if (uiState.value.roomInfo.deleted || uiState.value.roomInfo.blocked) {
            return
        }

        _unifiedMessages.update { current ->
            current.map { item ->
                if (item.chatMessage.chatElement.senderID == currentUserId &&
                    item.status == MessageStatus.SENT &&
                    !item.showReadStatus) {
                    item.copy(showReadStatus = true)
                } else {
                    item
                }
            }
        }
    }

    // 메시지 관련 util
    private fun addNewMessage(message: ChatMessageItem, status: MessageStatus) {
        _unifiedMessages.update { current ->
            val newItem = MessageItem(
                chatMessage = message,
                status = status,
                localId = generateLocalId(message.chatElement.content),
                showReadStatus = false
            )
            listOf(newItem) + current.take(REALTIME_MESSAGE_LIMIT)
        }
    }

    private fun findExistingMessageIndex(
        current: List<MessageItem>,
        message: ChatMessageItem
    ): Int {
        return current.indexOfFirst {
            it.chatMessage.chatElement.senderID == message.chatElement.senderID &&
                    it.chatMessage.chatElement.createdAt == message.chatElement.createdAt
        }
    }

    private fun updateExistingMessage(
        current: List<MessageItem>,
        index: Int,
        message: ChatMessageItem
    ): List<MessageItem> {
        return current.toMutableList().also { list ->
            list[index] = MessageItem(
                chatMessage = message,
                status = MessageStatus.RECEIVED,
                localId = list[index].localId,
                showReadStatus = false
            )
        }.toList()
    }

    private fun addNewMessageToList(
        current: List<MessageItem>,
        message: ChatMessageItem
    ): List<MessageItem> {
        val messageItem = MessageItem(
            chatMessage = message,
            status = MessageStatus.RECEIVED,
            localId = generateLocalId(message.chatElement.content),
            showReadStatus = false
        )
        return listOf(messageItem) + current.take(REALTIME_MESSAGE_LIMIT)
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

    private fun generateLocalId(content: String): String {
        val safeContent = content ?: "empty"
        return "local_${currentUserId}_${System.nanoTime()}_${localIdCounter.incrementAndGet()}_${safeContent.hashCode()}"
    }

    // api호출
    private fun loadInitialMessages(roomId: Long) {
        viewModelScope.launch {
            val pagingFlow = getChattingMessagesCurrentUseCase.invoke(roomId).cachedIn(viewModelScope)
            _messageState.update { it.copy(pagedMessages = pagingFlow) }
            _unifiedMessages.value = emptyList()
        }
    }

    private suspend fun loadChatRoomInfo(roomId: Long) {
        getChattingMessagesLastUseCase.invoke(roomId, 30)
            .onSuccess { chattingAll ->
                _uiState.update { it.copy(roomInfo = chattingAll.chatRoom) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(error = "Failed to load chat room info: ${e.message}") }
            }
    }

    // 메시지 상태
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

    // 스크롤 & UI
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

    //네비
    fun navigateToBack() {
        viewModelScope.launch { _naviEvent.emit(ChatNaviEvent.ToBack) }
    }

    fun navigateToProfile() {
        viewModelScope.launch { _naviEvent.emit(ChatNaviEvent.ToProfile) }
    }

    fun endChatRoom() {
        _unifiedMessages.value = emptyList()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val TAG = "ChattingViewModel"
        private const val REALTIME_MESSAGE_LIMIT = 50
    }
}