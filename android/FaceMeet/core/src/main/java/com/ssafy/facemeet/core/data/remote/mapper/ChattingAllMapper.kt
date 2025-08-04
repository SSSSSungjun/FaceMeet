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
        messages = messages.toDomain(),
        chatRoom = chatRoom.toDomain()
    )
}

fun ChatRoomResponse.toDomain(): ChatRoom {
    return ChatRoom(
        chatRoomID = chatRoomID,
        partnerID = partnerID,
        partnerNickname = partnerNickname,
        imgURL = this.imgURL ?: "",
        similar = similar
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun MessagesResponse.toDomain(): Messages {
    return Messages(
        messages = messages.map { it.toDomain() },
        currentPage = currentPage,
        totalPages = totalPages
    )
}