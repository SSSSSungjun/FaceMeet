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
import java.time.LocalDateTime
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
    private val _isInitialLoadCompleted = MutableStateFlow(false)

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
        Log.d(TAG, "🔄 liveMessages 업데이트: 임시=${tempMessages.size}, 실시간=${realtimeMessages.size}")
        (tempMessages.map { it.message } + realtimeMessages)
            .sortedByDescending { it.chatElement.createdAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


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
            // 실시간 메시지와 임시 메시지 초기화
            _tempMessages.value = emptyList()
            _realtimeMessages.value = emptyList()

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
            if(newMessage.chatElement.roomID!=currentRoomId)return@launch

            Log.d(TAG, "📩 메시지 처리 시작: ${newMessage.chatElement.content}")

            if (newMessage.chatElement.senderID == currentUserId) {
                removeTempMessage(newMessage)
            }

            val beforeCount = _realtimeMessages.value.size
            _realtimeMessages.update { currentList ->
                currentList + newMessage
            }
            Log.d(TAG, "✅ 실시간 메시지 추가: $beforeCount -> ${_realtimeMessages.value.size}")

            if (_uiState.value.scrollState.isAtBottom) {
                triggerScroll(ScrollEvent.ToBottom)
            }
        }
    }

    fun sendMessage() {
        Log.d(TAG, "sendMessage 호출됨")

        val state = _uiState.value
        Log.d(TAG, "canSendMessage: ${state.canSendMessage}, currentUserId: $currentUserId")

        if (!state.canSendMessage || currentUserId == 0L) {
            Log.w(TAG, "전송 불가 - canSend: ${state.canSendMessage}, userId: $currentUserId")
            return
        }

        val messageContent = state.messageText.trim()
        Log.d(TAG, "메시지 내용: '$messageContent'")

        val localId = generateLocalId(messageContent)

        // 임시 메시지 생성
        val tempMessage = createTempMessage(messageContent, state.roomInfo, localId)
        addTempMessage(tempMessage)
        Log.d(TAG, "임시 메시지 추가 완료")

        // UI 상태 업데이트
        _uiState.update {
            it.copy(
                messageText = "",
                canSendMessage = false,
                scrollState = it.scrollState.copy(isAtBottom = true)
            )
        }
        Log.d(TAG, "UI 상태 업데이트 완료")

        // 자동 스크롤
        triggerScroll(ScrollEvent.ToBottom)

        // 웹소켓으로 메시지 전송
        viewModelScope.launch {
            try {
                Log.d(TAG, "웹소켓 메시지 전송 시작")
                Log.d(
                    TAG,
                    "roomId: ${state.roomInfo.chatRoomID}, senderId: $currentUserId, receiverId: ${state.roomInfo.partnerID}"
                )

                webSocketManager.sendMessage(
                    content = messageContent,
                    roomId = state.roomInfo.chatRoomID,
                    senderId = currentUserId,
                    receiverId = state.roomInfo.partnerID,
                    tempId = System.currentTimeMillis()
                )
                Log.d(TAG, "웹소켓 메시지 전송 완료")

                // 타임아웃 후 임시 메시지 제거
                delay(TEMP_MESSAGE_TIMEOUT)
                removeTempMessageById(localId)

            } catch (e: Exception) {
                Log.e(TAG, "메시지 전송 실패", e)
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

    private fun cleanupMessagesIfNeeded() {
        val currentCount = _realtimeMessages.value.size
        if (currentCount > REALTIME_MESSAGE_LIMIT) {
            Log.d(TAG, "메시지 정리 시작: ${currentCount}개")
            _realtimeMessages.update { currentList ->
                currentList.sortedBy { it.chatElement.createdAt }
                    .takeLast(REALTIME_MESSAGE_LIMIT)
            }
            Log.d(TAG, "메시지 정리 완료, 남은 개수: ${_realtimeMessages.value.size}개")
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

                cleanupMessagesIfNeeded()
            }
        }
    }

    // 유틸리티 메서드들
    private fun generateLocalId(content: String): String {
        return "${currentUserId}_${System.currentTimeMillis()}_${content.hashCode()}"
    }

    private fun createTempMessage(
        content: String,
        roomInfo: ChatRoom,
        localId: String
    ): TempMessage {
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
            val beforeCount = currentList.size
            val afterList = currentList.filter { temp ->
                val isSameMessage = temp.message.chatElement.content.trim() == realMessage.chatElement.content.trim() &&
                        temp.message.chatElement.senderID == realMessage.chatElement.senderID &&
                        temp.message.chatElement.roomID == realMessage.chatElement.roomID

                !isSameMessage // 같은 메시지가 아닌 것만 남김
            }

            Log.d(TAG, "임시 메시지 제거: $beforeCount -> ${afterList.size}")
            afterList
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
        getChattingMessagesLastUseCase.invoke(roomId, 30).onSuccess { chattingAll ->
            _uiState.update { it.copy(roomInfo = chattingAll.chatRoom) }
        }
    }

    fun connectToChat() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "connectToChat: $currentRoomId")
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

    fun navigateToProfile() {
        viewModelScope.launch {
            _naviEvent.emit(ChatNaviEvent.ToProfile)
        }
    }

    override fun onCleared() {
        super.onCleared()
        endChatRoom()
    }
}
