package com.ssafy.facemeet.client.ui.chat

data class ChatUiState(
    val currentUserId: Long = 0L,
    val roomId: Int = 0,
    val receiverId: Long = 0L,
    val messageText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val canSendMessage: Boolean = false
)
