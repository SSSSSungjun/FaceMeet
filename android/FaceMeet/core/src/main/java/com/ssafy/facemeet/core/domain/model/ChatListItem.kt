package com.ssafy.facemeet.core.domain.model

data class ChatListItem(
    val userId: Int,
    val nickName: String,
    val lastSeen: String,
    val isOnline: Boolean,
    val chatRoomId: Int,
    val chatRoomStringId: String,
    val lastMessage: String,
    val nonReadCnt: Int,
    val blocked: Boolean
)
