package com.ssafy.facemeet.core.domain.model

data class TakeMatchTicket(
    val settingId: Long,
    val acquiredAt: String?,
    val remainingCount: Int,
    val message: String?,
    val success: Boolean
)