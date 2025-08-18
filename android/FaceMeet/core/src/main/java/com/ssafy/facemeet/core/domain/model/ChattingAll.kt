package com.ssafy.facemeet.core.domain.model

data class ChattingAll (
    val messages: Messages,
    val chatRoom: ChatRoom
)

data class ChatRoom (
    val chatRoomID: Long=0L,
    val partnerID: Long=0L,
    val partnerNickname: String="",
    val blocked : Boolean=false,
    val deleted : Boolean=false,
    val imgURL: String="",
    val similar: Long=0L
)

data class Messages (
    val messages: List<ChatElement> =listOf(),
    val currentPage: Long= 0L ,
    val totalPages: Long= 0L
)

