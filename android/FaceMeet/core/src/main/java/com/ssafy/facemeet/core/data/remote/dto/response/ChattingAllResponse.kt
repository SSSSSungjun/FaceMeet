package com.ssafy.facemeet.core.data.remote.dto.response

data class ChattingAllResponse(
    val messages: MutableList<ChatElementResponse>,
    val totalPages : Int,
)
