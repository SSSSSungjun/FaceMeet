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
    private val getChattingMessagesCurrentUseCase: GetChattingMessagesCurrentUseCase,
    private val getChattingListUseCase: GetChattingListUseCase,
) : ViewModel() {

    // 현재 사용자 및 방 정보
    internal var currentUserId: Long = 0L
    private var currentRoomId: Long = 0L
    private var currentPartnerId : Long = 0L

    // UI 상태 관리
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _listUiState = MutableStateFlow(ChatListUiState())
    val listUiState = _listUiState.asStateFlow()

    // 메시지 관련 상태 관리
    private val _messageState = MutableStateFlow(MessageState())
    val messageState: StateFlow<MessageState> = _messageState.asStateFlow()

    // UI 이벤트 (네비게이션, 스크롤)
    private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
    val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

    private val _scrollEvent = MutableSharedFlow<ScrollEvent>()
    val scrollEvent: SharedFlow<ScrollEvent> = _scrollEvent.asSharedFlow()

    // WebSocket 연결 상태
    val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

    // 통합 메시지 리스트
    private val _unifiedMessages = MutableStateFlow<List<MessageItem>>(emptyList())
    val unifiedMessages: StateFlow<List<MessageItem>> = _unifiedMessages.asStateFlow()

    private val _isScreenActive = MutableStateFlow(false)
    val isScreenActive: StateFlow<Boolean> = _isScreenActive.asStateFlow()

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

    init {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }
    }

    // 채팅방 초기화 및 연결
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

    // 초기 메시지 로드 (페이징)
    private fun loadInitialMessages(roomId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load initial messages: roomId=$roomId")
            val pagingFlow = getChattingMessagesCurrentUseCase.invoke(roomId)
                .cachedIn(viewModelScope)
            _messageState.update { it.copy(pagedMessages = pagingFlow) }
            _unifiedMessages.value = emptyList()
            Log.d(TAG, "Initial message load complete")
        }
    }



    // WebSocket 콜백 설정
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
           //updateReadStatusInUI()
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

           // updateReadStatusInUI()
        }
//        webSocketManager.onNewMessageForList = {
//            viewModelScope.launch {
//                getChattingListUseCase().onSuccess { chatList ->
//                    Log.d(TAG, "loadChattingList: 로드 성공 $chatList")
//                    _listUiState.value = _listUiState.value.copy(
//                        chatList = chatList,
//                        isLoading = false
//                    )
//                }.onFailure {
//                    Log.d(TAG, "${it.message} 방 리스트 불러오기 실패")
//                }
//            }
//        }
    }

    // 새 메시지 수신 처리
    private fun handleNewMessage(newMessage: ChatMessageItem) {
        viewModelScope.launch {

            if (newMessage.chatElement.roomID != currentRoomId || newMessage.chatElement.senderID == currentUserId) {
                Log.w(TAG, "Ignoring message for a different room or from self.")
                return@launch
            }

            Log.d(TAG, "📩 Processing new message from partner: ${newMessage.chatElement.content}")

            val messageKey = generateMessageKey(newMessage.chatElement)
            if (_unifiedMessages.value.any { generateMessageKey(it.chatMessage.chatElement) == messageKey }) {
                Log.d(TAG, "Ignoring duplicate message: $messageKey")
                return@launch
            }

            addNewMessage(newMessage, MessageStatus.RECEIVED)
            if (newMessage.chatElement.senderID != currentUserId && !uiState.value.scrollState.isAtBottom) {
                _uiState.update { it.copy(newMessageContent = newMessage.chatElement.content) }
            }

            if (_isScreenActive.value) { // 이 블록을 다시 추가
                Log.d(TAG, "🟢 handleNewMessage: 화면 활성화 상태, markAsRead() 호출 시작")
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

        val sentMessage = createSentMessage(state.messageText.trim(), state.roomInfo)
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

    // 새 메시지를 리스트에 추가
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

    // 메시지 읽음 처리
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
                // 내가 보낸 메시지이고, 아직 읽음 처리되지 않은 메시지라면
                // showReadStatus를 true로 변경합니다.
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
        val messageItem = ChatMessageItem(chatElement = chatElement, messageType = MessageType.TEXT)
        return MessageItem(chatMessage = messageItem, status = MessageStatus.SENT)
    }

    // 메시지 키 생성
    private fun generateMessageKey(chatElement: ChatElement): String {
        return "${chatElement.senderID}_${chatElement.roomID}_${chatElement.content}_${chatElement.createdAt}"
    }

    // 로컬 ID 생성
    private fun generateLocalId(content: String): String {
        val safeContent = content ?: "empty"
        return "local_${currentUserId}_${System.currentTimeMillis()}_${safeContent.hashCode()}"
    }

    // 스크롤 상태 변경 처리
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
        _uiState.update { currentState ->
            currentState.copy(
                scrollState = currentState.scrollState.copy(isAtBottom = true),
                newMessageContent = null
            )
        }
        triggerScroll(ScrollEvent.ToBottom)
    }

    // 스크롤 이벤트 트리거
    private fun triggerScroll(event: ScrollEvent) {
        viewModelScope.launch { _scrollEvent.emit(event) }
    }

    // 공지사항 토글
    fun toggleNotice() {
        _uiState.update { it.copy(isNoticeOpen = !it.isNoticeOpen) }
    }

    // 채팅방 정보 로드
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

    // 채팅 연결
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

    // 메시지 텍스트 업데이트
    fun updateMessageText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                messageText = text,
                canSendMessage = text.isNotBlank() && currentState.connectionState == ConnectionState.CONNECTED
            )
        }
    }

    // 연결 상태 업데이트
    fun updateConnectionState(connectionState: ConnectionState) {
        _uiState.update { currentState ->
            currentState.copy(
                connectionState = connectionState,
                canSendMessage = currentState.messageText.isNotBlank() && connectionState == ConnectionState.CONNECTED
            )
        }
    }

    // 채팅방 종료
    fun endChatRoom() {
        _unifiedMessages.value = emptyList()
    }

    // 에러 초기화
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // 네비게이션
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