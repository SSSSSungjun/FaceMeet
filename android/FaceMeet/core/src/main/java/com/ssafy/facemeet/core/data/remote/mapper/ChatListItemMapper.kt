package com.ssafy.facemeet.core.data.remote.mapper

import com.ssafy.facemeet.core.data.remote.dto.response.ChatListItemResponse
import com.ssafy.facemeet.core.domain.model.ChatListItem

fun ChatListItemResponse.toDomain() : ChatListItem{
    return ChatListItem(
        userId = this.userId,
        nickName = this.nickName,
        lastSeen = this.lastSeen,
        isOnline = this.isOnline,
        chatRoomId = this.chatRoomId,
        chatRoomStringId = this.chatRoomStringId,
        lastMessage = this.lastMessage,
        nonReadCnt = this.nonReadCnt,
        blocked = this.blocked
    )
}