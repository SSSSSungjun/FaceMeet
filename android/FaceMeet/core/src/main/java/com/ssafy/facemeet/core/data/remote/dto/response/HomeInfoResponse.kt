package com.ssafy.facemeet.core.data.remote.dto.response

data class HomeInfoResponse(
    val img: String?,
    val nickname: String,
    val title: String,
    val remainingMatchTickets: Int
)