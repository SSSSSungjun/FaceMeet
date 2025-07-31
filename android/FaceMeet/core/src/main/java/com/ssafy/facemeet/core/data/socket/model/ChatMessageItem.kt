package com.ssafy.facemeet.core.data.socket.model

import com.ssafy.facemeet.core.data.remote.dto.response.ChatElementResponse

data class ChatMessageItem(
    val chatElement: ChatElementResponse,
    val messageType: MessageType = MessageType.TEXT
)