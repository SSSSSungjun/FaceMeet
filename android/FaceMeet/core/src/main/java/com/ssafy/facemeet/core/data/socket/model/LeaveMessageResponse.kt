package com.ssafy.facemeet.core.data.socket.model

data class LeaveMessageResponse(
    val type: String,
    val message: String,
    val roomId: Long
)