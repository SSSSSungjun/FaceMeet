package com.ssafy.facemeet.core.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("notificationId") val notificationId: Long,
    @SerializedName("settingId") val settingId: Long?,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("messageData") val messageData: MessageData? = null,
    @SerializedName("isRead") val isRead: Boolean
)

data class MessageData(
    @SerializedName("body") val body: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("settingId") val settingId: String? = null,   // 서버가 문자열로 줌
    @SerializedName("timestamp") val timestamp: String? = null,   // 예: 2025-08-15T14:46:28.223198123
    @SerializedName("triggerTime") val triggerTime: String? = null, // 예: 2025-08-15T14:47
    @SerializedName("type") val type: String? = null              // 🔹응답엔 있지만 UI에선 사용 X
)
