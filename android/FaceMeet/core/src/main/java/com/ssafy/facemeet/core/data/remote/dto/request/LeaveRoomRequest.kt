package com.ssafy.facemeet.core.data.remote.dto.request

data class LeaveRoomRequest(
    val userId: Long,
    val partnerId: Long,
    val roomId: Long
)