package com.ssafy.facemeet.core.data.remote.dto.request

data class ChatMessageRequest(
    val roomId: Long,
    val senderId: Long,
    val receiverId: Long,
    val content: String
)