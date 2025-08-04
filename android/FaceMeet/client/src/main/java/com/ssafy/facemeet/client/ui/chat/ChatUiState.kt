package com.ssafy.facemeet.client.ui.chat

import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.domain.model.ChatRoom

data class ChatUiState(
    val messageText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val canSendMessage: Boolean = false,
    val chattingAllList : List<ChatMessageItem> = listOf(),
    val roomInfo : ChatRoom = ChatRoom()
)
