package com.ssafy.facemeet.core.data.remote.dto.request

data class ReportRequest(
    val roomId: Long,
    val categoryId: Long,
    val reportedId: Long,
    val reason: String,
    val img: String = ""
)
