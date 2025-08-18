package com.ssafy.facemeet.core.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class TakeMatchTicketResponse(
    @SerializedName(value = "success", alternate = ["sucess"]) val success: Boolean = false,
    val message: String? = null,
    val settingId: Long = 0L,
    val userId: Long? = null,
    val acquiredAt: String? = null,
    val remainingCount: Int? = null
)