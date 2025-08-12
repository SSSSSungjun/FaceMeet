package com.ssafy.facemeet.core.data.remote.mapper

import com.ssafy.facemeet.core.data.remote.dto.response.ChatListItemResponse
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.util.format.ParsingTimeData.formatSmartDate

fun ChatListItemResponse.toDomain(): ChatListItem {
    return ChatListItem(
        userId = this.userId,
        nickName = this.nickName ?: "",
        lastActivatedTime = this.lastActivatedTime?.formatSmartDate() ?: "", //이거 필요있음? 필요없음
        isOnline = this.isOnline,
        imgUrl = this.imgUrl ?:"",
        chatRoomId = this.chatRoomId,
        chatRoomStringId = this.chatRoomStringId ?: "",
        lastMessage = this.lastMessage ?: "",
        lastSendMessageTime = this.lastSendMessageTime?.formatSmartDate() ?: "",
        nonReadCnt = this.nonReadCnt,
        blocked = this.blocked,
        deleted = this.deleted
    )
}
