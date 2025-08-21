package com.ssafy.facemeet.core.data.remote.dto.response

data class LeaveMessageResponse(
    val type: String,
    val message: String,
    val roomId: Long
)