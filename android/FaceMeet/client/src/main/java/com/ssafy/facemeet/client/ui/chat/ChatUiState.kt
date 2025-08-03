package com.ssafy.facemeet.client.ui.chat

import com.ssafy.facemeet.core.domain.model.ChattingAll

data class ChatUiState(
    val currentUserId: Long = 0L,
    val roomId: Long = 0L,
    val receiverId: Long = 0L,
    val messageText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val canSendMessage: Boolean = false,
    val chattingAllList : ChattingAll = ChattingAll(mutableListOf(),0)
)
