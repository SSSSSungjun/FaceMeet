package com.ssafy.facemeet.core.data.remote.dto.request

data class MarkAsReadRequest(
    val readerId: Long,
    val senderId: Long,
    val roomId: Long
)