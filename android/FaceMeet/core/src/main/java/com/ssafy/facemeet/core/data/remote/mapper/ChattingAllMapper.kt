package com.ssafy.facemeet.core.data.remote.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.dto.response.ChatRoomResponse
import com.ssafy.facemeet.core.data.remote.dto.response.ChattingAllResponse
import com.ssafy.facemeet.core.data.remote.dto.response.MessagesResponse
import com.ssafy.facemeet.core.domain.model.ChatRoom
import com.ssafy.facemeet.core.domain.model.ChattingAll
import com.ssafy.facemeet.core.domain.model.Messages

@RequiresApi(Build.VERSION_CODES.O)
fun ChattingAllResponse.toDomain(): ChattingAll {
    return ChattingAll(
        messages = messages?.toDomain() ?: Messages(emptyList(), 0, 0),
        chatRoom = chatRoom?.toDomain() ?: ChatRoom(
            chatRoomID = 0L,
            partnerID = 0L,
            partnerNickname = "",
            imgURL = "",
            similar = 0
        )
    )
}

fun ChatRoomResponse.toDomain(): ChatRoom {
    return ChatRoom(
        chatRoomID = chatRoomID ?: 0L,
        partnerID = partnerID ?: 0L,
        partnerNickname = partnerNickname ?: "",
        imgURL = imgURL ?: "",
        similar = similar ?: 0
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun MessagesResponse.toDomain(): Messages {
    return Messages(
        messages = messages?.mapNotNull { it?.toDomain() } ?: emptyList(),
        currentPage = currentPage ?: 0,
        totalPages = totalPages ?: 0
    )
}