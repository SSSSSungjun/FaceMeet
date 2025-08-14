package com.ssafy.facemeet.core.data.socket.model

data class ReadReceiptData(
    val type: String,
    val roomId: Long,
    val readerId: Long,
    val readAt: String,
    val message: String
)