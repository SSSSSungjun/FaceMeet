package com.ssafy.facemeet.core.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ChattingAllResponse (
    val messages: MessagesResponse,
    val chatRoom: ChatRoomResponse
)

data class ChatRoomResponse (
    @SerializedName("chatRoomId")
    val chatRoomID: Long,
    @SerializedName("partnerId")
    val partnerID: Long,
    val partnerNickname: String,
    @SerializedName("imgUrl")
    val imgURL: String?,
    val similar: Long
)

data class MessagesResponse (
    val messages: List<ChatElementResponse>,
    val currentPage: Long,
    val totalPages: Long
)