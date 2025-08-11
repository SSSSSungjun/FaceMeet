package com.ssafy.facemeet.client.ui.chat.model

import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.domain.model.ChatRoom

data class ChatUiState(
    val roomInfo: ChatRoom = ChatRoom(),
    val messageText: String = "",
    val canSendMessage: Boolean = false,
    val isLoading: Boolean = true,
    val isNoticeOpen: Boolean = false,
    val error: String? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val scrollState: ScrollState = ScrollState()
)

data class ScrollState(
    val isUserScrolling: Boolean = false,
    val isAtBottom: Boolean = true,
    val shouldAutoScroll: Boolean = false,
    val shouldScrollWithKeyboard: Boolean = false
)

