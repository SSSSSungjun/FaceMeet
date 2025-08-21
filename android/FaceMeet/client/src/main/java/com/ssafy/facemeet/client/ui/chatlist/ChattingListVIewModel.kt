package com.ssafy.facemeet.client.ui.chatlist

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.domain.usecase.ConnectChatWebSocketUseCase
import com.ssafy.facemeet.core.domain.usecase.DisconnectChatWebSocketUseCase
import com.ssafy.facemeet.core.domain.usecase.GetChattingListUseCase
import com.ssafy.facemeet.core.domain.usecase.LeaveChatRoomUseCase
import com.ssafy.facemeet.core.domain.usecase.ObserveChatEventsUseCase
import com.ssafy.facemeet.core.domain.usecase.PostChattingLeaveUseCase
import com.ssafy.facemeet.core.util.AppStateManager
import com.ssafy.facemeet.core.util.messaging.ChatMessageItem
import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChattingListViewModel"

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ChattingListViewModel @Inject constructor(
    private val connectChatWebSocketUseCase: ConnectChatWebSocketUseCase,
    private val disconnectChatWebSocketUseCase: DisconnectChatWebSocketUseCase,
    private val leaveChatRoomUseCase: LeaveChatRoomUseCase,
    private val observeChatEventsUseCase: ObserveChatEventsUseCase,

    private val tokenManager: TokenManager,
    private val getChattingListUseCase: GetChattingListUseCase,
    private val postChattingLeaveUseCase: PostChattingLeaveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState

    internal var selectedRoomId: Long = 0L
    private var currentUserId: Long = 0L

    init {
        AppStateManager.setCurrentScreen("ChattingListScreen")
        observeWebSocketEvents()
        viewModelScope.launch {
            currentUserId = tokenManager.getUserPK()?.toLong() ?: 0L
        }
    }

    private fun observeWebSocketEvents() {
        observeChatEventsUseCase()
            .onEach { event ->
                when (event) {
                    is WebSocketEvent.NewMessage -> {
                        Log.d(TAG, "새로운 메시지 수신 - 채팅 리스트 새로고침!")
                        // 새 메시지가 왔을 때 채팅 리스트 갱신
                        if (shouldRefreshChatList(event.messageItem)) {
                            loadChattingList()
                        }
                    }

                    is WebSocketEvent.UserLeft -> {
                        Log.d(TAG, "사용자 나가기 이벤트 - 채팅 리스트 새로고침!")
                        loadChattingList()
                    }

                    is WebSocketEvent.ConnectionStateChanged -> {
                        Log.d(TAG, "WebSocket 연결 상태 변경: ${event.state}")
                        _uiState.value = _uiState.value.copy(
                            isWebSocketConnected = event.state.name
                        )
                    }

                    is WebSocketEvent.WebSocketError -> {
                        Log.e(TAG, "WebSocket 오류: ${event.throwable.message}")
                        _uiState.value = _uiState.value.copy(
                            error = "Connection error: ${event.throwable.message}"
                        )
                    }

                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    private fun shouldRefreshChatList(messageItem: ChatMessageItem): Boolean {
        return messageItem.chatElement.senderID != currentUserId
    }

    fun initializeChatting() {
        viewModelScope.launch {
            Log.d(TAG, "채팅 리스트 WebSocket 연결 초기화 시작")

            disconnectChatWebSocketUseCase()

            val token = tokenManager.getAccessToken()
            if (token != null && currentUserId != 0L) {
                connectChatWebSocketUseCase.invoke(currentUserId, token)
                Log.d(TAG, "WebSocket 연결 요청 완료: userId=$currentUserId")
            } else {
                Log.e(TAG, "토큰 또는 사용자 ID가 없습니다.")
                _uiState.value = _uiState.value.copy(
                    error = "인증 정보가 없습니다."
                )
            }

            loadChattingList()
        }
    }

    fun loadChattingList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getChattingListUseCase()
                .onSuccess { chatList ->
                    Log.d(TAG, "loadChattingList: 로드 성공 - ${chatList.size}개 채팅방")
                    _uiState.value = _uiState.value.copy(
                        chatList = chatList.toMutableList(),
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    Log.e(TAG, "채팅 리스트 불러오기 실패: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "채팅 리스트를 불러오는데 실패했습니다: ${exception.message}"
                    )
                }
        }
    }

    fun clickExitRoom() {
        _uiState.value = _uiState.value.copy(showExitDialog = true)
    }

    fun dismissExitDialog() {
        _uiState.value = _uiState.value.copy(showExitDialog = false)
    }

    fun setSelectedRoomId(roomId: Long) {
        selectedRoomId = roomId
        Log.d(TAG, "선택된 방 ID 설정: $roomId")
    }

    fun exitRoom(chatRoomId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val partnerId = getPartnerIdFromChatRoom(chatRoomId)
                if (partnerId != null) {
                    leaveChatRoomUseCase.invoke(currentUserId, partnerId, chatRoomId)
                    Log.d(TAG, "WebSocket 방 나가기 요청 전송: roomId=$chatRoomId, partnerId=$partnerId")
                }

                postChattingLeaveUseCase(chatRoomId)
                    .onSuccess {
                        Log.d(TAG, "방 나가기 성공: roomId=$chatRoomId")
                        dismissExitDialog()
                        loadChattingList() // 리스트 새로고침
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "방 나가기 실패: ${exception.message}")
                        _uiState.value = _uiState.value.copy(
                            error = "방 나가기에 실패했습니다: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "방 나가기 처리 중 오류: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "방 나가기 처리 중 오류가 발생했습니다: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun getPartnerIdFromChatRoom(chatRoomId: Long): Long? {
        return _uiState.value.chatList.find { it.chatRoomId == chatRoomId }?.userId
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshChatList() {
        loadChattingList()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel 정리 - WebSocket 연결 해제")
        AppStateManager.clearCurrentScreen()

        disconnectChatWebSocketUseCase()
    }
}

