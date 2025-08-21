package com.ssafy.facemeet.client.ui.chatlist

import com.ssafy.facemeet.core.domain.model.ChatListItem

data class ChatListUiState(
    val chatList: List<ChatListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showExitDialog: Boolean = false,
    val isWebSocketConnected: String = "DISCONNECTED",
    val lastRefreshTime: Long = 0L
)
