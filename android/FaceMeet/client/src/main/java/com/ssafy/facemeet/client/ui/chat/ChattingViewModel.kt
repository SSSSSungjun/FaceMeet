package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesCurrentUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingMessagesLastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

private const val TAG = "ChattingViewModel"

// ViewModel 수정
@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChattingViewModel @Inject constructor(
    private val webSocketManager: ChatWebSocketManager,
    private val tokenManager: TokenManager,
    private val getChattingMessagesLastUseCase: GetChattingMessagesLastUseCase,
    private val getChattingMessagesCurrentUseCase: GetChattingMessagesCurrentUseCase
) : ViewModel() {

    var currentUserId: Long = 0L

    private var _pagedMessages = MutableStateFlow<Flow<PagingData<ChatMessageItem>>>(flowOf(PagingData.empty()))
    val pagedMessages: StateFlow<Flow<PagingData<ChatMessageItem>>> = _pagedMessages

    private val _realtimeMessages = MutableStateFlow<List<ChatMessageItem>>(emptyList())
    val realtimeMessages: StateFlow<List<ChatMessageItem>> = _realtimeMessages

    data class TempMessage(
        val message: ChatMessageItem,
        val sendTimestamp: Long,
        val localId: String
    )

    private val _tempMessages = MutableStateFlow<List<TempMessage>>(emptyList())
    val tempMessages: StateFlow<List<ChatMessageItem>> = _tempMessages.map { tempList ->
        tempList.map { it.message }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val _naviEvent = MutableSharedFlow<ChatNaviEvent?>()
    val naviEvent: SharedFlow<ChatNaviEvent?> = _naviEvent.asSharedFlow()

    private val _scrollToBottom = MutableSharedFlow<Unit>()
    val scrollToBottom: SharedFlow<Unit> = _scrollToBottom.asSharedFlow()

    // 키보드와 함께 스크롤하기 위한 이벤트
    private val _scrollWithKeyboard = MutableSharedFlow<Unit>()
    val scrollWithKeyboard: SharedFlow<Unit> = _scrollWithKeyboard.asSharedFlow()

    val connectionState: LiveData<ConnectionState> = webSocketManager.connectionState

    private var isUserScrolling = false
    private var isUserAtBottom = true
    private var isInitialLoad = true // 최초 로드 여부

    init {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }
    }

    fun initializeChat(roomId: Long) {
        webSocketManager.disconnect()
        viewModelScope.launch {
            loadChatRoomInfo(roomId)
            isInitialLoad = true // 초기 로드 플래그 설정
            loadInitialPagingData(roomId)
            setupWebSocketCallbacks()
            connectToChat()
        }
    }

    private fun loadInitialPagingData(roomId: Long) {
        viewModelScope.launch {
            val pagingFlow = getChattingMessagesCurrentUseCase.invoke(roomId)
                .cachedIn(viewModelScope)
            _pagedMessages.value = pagingFlow

            // 실시간 메시지 초기화
            _realtimeMessages.value = emptyList()
            _tempMessages.value = emptyList()

            Log.d("ChattingViewModel", "초기 페이징 데이터 로드 완료")
        }
    }

    private fun setupWebSocketCallbacks() {
        webSocketManager.setOnNewMessageCallback { newMessage ->
            handleNewWebSocketMessage(newMessage)
        }
    }

    private fun handleNewWebSocketMessage(newMessage: ChatMessageItem) {
        viewModelScope.launch {
            if (newMessage.chatElement.senderID == currentUserId) {
                removeTempMessage(newMessage)
            }

            _realtimeMessages.update { currentList ->
                currentList + newMessage
            }

            // 새 메시지가 오면 무조건 맨 아래로 (상대방 메시지든 내 메시지든)
            if (isUserAtBottom && !isUserScrolling) {
                _scrollToBottom.emit(Unit)
            }

            Log.d("ChattingViewModel", "새 메시지 추가: ${newMessage.chatElement.content}")
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        if (!state.canSendMessage || currentUserId == 0L) return

        val messageContent = state.messageText.trim()
        val sendTimestamp = System.currentTimeMillis()
        val localId = "${currentUserId}_${sendTimestamp}_${messageContent.hashCode()}"

        val tempChatElement = ChatElement(
            content = messageContent,
            senderID = currentUserId,
            receiverID = state.roomInfo.partnerID,
            roomID = state.roomInfo.chatRoomID,
            createdAt = ZonedDateTime.now().toString(),
            isRead = false,
            readAt = ""
        )

        val tempMessageItem = ChatMessageItem(
            chatElement = tempChatElement,
            messageType = MessageType.TEXT
        )

        val tempMessage = TempMessage(
            message = tempMessageItem,
            sendTimestamp = sendTimestamp,
            localId = localId
        )

        _tempMessages.update { currentList ->
            currentList + tempMessage
        }

        _uiState.update {
            it.copy(messageText = "", canSendMessage = false)
        }

        // 메시지 전송 시 무조건 맨 아래로
        viewModelScope.launch {
            isUserAtBottom = true
            _scrollToBottom.emit(Unit)
        }

        viewModelScope.launch {
            try {
                webSocketManager.sendMessage(
                    content = messageContent,
                    roomId = state.roomInfo.chatRoomID,
                    senderId = currentUserId,
                    receiverId = state.roomInfo.partnerID
                )

                delay(10000)
                removeTempMessageByLocalId(localId)

            } catch (e: Exception) {
                removeTempMessageByLocalId(localId)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun removeTempMessage(realMessage: ChatMessageItem) {
        _tempMessages.update { currentList ->
            currentList.filter { temp ->
                val isMatching = temp.message.chatElement.content == realMessage.chatElement.content &&
                        temp.message.chatElement.senderID == realMessage.chatElement.senderID &&
                        temp.message.chatElement.roomID == realMessage.chatElement.roomID

                if (isMatching) {
                    Log.d("ChattingViewModel", "임시 메시지 제거됨: ${temp.message.chatElement.content}")
                }

                !isMatching
            }
        }
    }

    private fun removeTempMessageByLocalId(localId: String) {
        _tempMessages.update { currentList ->
            currentList.filter { it.localId != localId }
        }
    }

    fun onUserScrollStart() {
        isUserScrolling = true
    }

    fun onUserScrollEnd() {
        isUserScrolling = false
    }

    fun onScrollPositionChanged(firstVisibleItemIndex: Int, totalItemCount: Int) {
        isUserAtBottom = firstVisibleItemIndex <= 2
    }

    // 최초 로드 완료 시 호출
    fun onInitialLoadComplete() {
        isInitialLoad = false
        viewModelScope.launch {
            _scrollToBottom.emit(Unit)
        }
    }

    // 키보드가 올라올 때 호출
    fun onKeyboardShown() {
        if (isUserAtBottom) {
            // 맨 아래에 있을 때만 키보드와 함께 스크롤
            viewModelScope.launch {
                _scrollWithKeyboard.emit(Unit)
            }
        }
        // 위쪽에 있으면 현재 위치 유지
    }

    fun scrollToBottomManually() {
        viewModelScope.launch {
            isUserAtBottom = true
            _scrollToBottom.emit(Unit)
        }
    }

    fun refreshChatManually() {
        viewModelScope.launch {
            val currentRoomId = _uiState.value.roomInfo.chatRoomID
            if (currentRoomId != 0L) {
                Log.d("ChattingViewModel", "수동 페이징 새로고침")

                _realtimeMessages.value = emptyList()

                val newPagingFlow = getChattingMessagesCurrentUseCase.invoke(currentRoomId)
                    .cachedIn(viewModelScope)
                _pagedMessages.value = newPagingFlow
            }
        }
    }

    fun cleanupRealtimeMessages() {
        viewModelScope.launch {
            val currentCount = _realtimeMessages.value.size
            if (currentCount > 50) {
                Log.d("ChattingViewModel", "실시간 메시지 정리: ${currentCount}개 → 페이징에 통합")
                refreshChatManually()
            }
        }
    }

    suspend fun loadChatRoomInfo(roomId: Long) {
        getChattingMessagesLastUseCase.invoke(roomId, 30).onSuccess { chattingAll ->
            Log.d(TAG, "loadChatRoomInfo:$chattingAll ")
            _uiState.update {
                it.copy(roomInfo = chattingAll.chatRoom)
            }
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
        _tempMessages.value = emptyList()
        _realtimeMessages.value = emptyList()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
        _tempMessages.value = emptyList()
        _realtimeMessages.value = emptyList()
    }

    fun navigateToBack() {
        viewModelScope.launch {
            _naviEvent.emit(ChatNaviEvent.ToBack)
        }
    }
}
