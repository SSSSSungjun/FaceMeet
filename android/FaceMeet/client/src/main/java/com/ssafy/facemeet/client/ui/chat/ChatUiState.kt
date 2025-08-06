package com.ssafy.facemeet.client.ui.chat

import androidx.paging.PagingData
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.domain.model.ChatRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


data class ChatUiState(
    val roomInfo: ChatRoom = ChatRoom(),
    val messageText: String = "",
    val canSendMessage: Boolean = false,
    val isLoading: Boolean = true,
    val isNoticeOpen: Boolean = true,
    val error: String? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val scrollState: ScrollState = ScrollState()
)


data class MessageState(
    val pagedMessages: Flow<PagingData<ChatMessageItem>> = flowOf(PagingData.empty())
)

data class ScrollState(
    val isUserScrolling: Boolean = false,
    val isAtBottom: Boolean = true,
    val shouldAutoScroll: Boolean = false,
    val shouldScrollWithKeyboard: Boolean = false
)

