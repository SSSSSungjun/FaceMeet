package com.ssafy.facemeet.core.domain.model

data class ChatElement(
    val content: String,
    val senderID: Long,
    val receiverID: Long,
    val roomID: String,
    val createdAt: String,
    val isRead: Boolean,
    val readAt: String
)
