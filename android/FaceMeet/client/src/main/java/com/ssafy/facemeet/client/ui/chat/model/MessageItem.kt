package com.ssafy.facemeet.client.ui.chat.model

import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem

data class MessageItem(
    val chatMessage: ChatMessageItem,
    val status: MessageStatus = MessageStatus.RECEIVED,
    val localId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val showReadStatus: Boolean = false
)

enum class MessageStatus { PENDING, SENT, FAILED, RECEIVED }