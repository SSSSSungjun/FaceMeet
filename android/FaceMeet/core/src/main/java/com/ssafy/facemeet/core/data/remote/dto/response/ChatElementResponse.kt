package com.ssafy.facemeet.core.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatElementResponse(
    val content: String,

    @SerialName("senderId")
    val senderID: Long,

    @SerialName("receiverId")
    val receiverID: Long,

    @SerialName("roomId")
    val roomID: String,

    val createdAt: String,
    val isRead: Boolean,
    val readAt: String
)
