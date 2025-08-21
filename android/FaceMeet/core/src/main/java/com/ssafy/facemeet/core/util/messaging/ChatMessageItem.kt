package com.ssafy.facemeet.core.util.messaging

import com.ssafy.facemeet.core.domain.model.ChatElement

data class ChatMessageItem(
    val chatElement: ChatElement,
    val messageType: MessageType = MessageType.TEXT
)