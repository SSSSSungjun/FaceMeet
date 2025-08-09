package com.ssafy.facemeet.core.domain.model

data class ChatListItem(
    val userId: Long,
    val nickName: String,
    val lastActivatedTime: String?,
    val isOnline: Boolean,
    val imgUrl : String,
    val chatRoomId: Long,
    val chatRoomStringId: String,
    val lastMessage: String,
    val lastSendMessageTime: String?,
    val nonReadCnt: Int,
    val blocked: Boolean,
    val deleted: Boolean
)
