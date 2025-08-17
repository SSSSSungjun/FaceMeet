package com.ssafy.facemeet.core.data.socket.model

import com.ssafy.facemeet.core.domain.model.ChatElement

data class ChatMessageItem(
    val chatElement: ChatElement,
    val messageType: MessageType = MessageType.TEXT
)