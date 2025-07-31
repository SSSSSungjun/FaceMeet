package com.ssafy.facemeet.core.data.socket.model

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketSendMessage(
    val roomId: String,
    val senderId: Long,
    val receiverId: Long,
    val content: String
)