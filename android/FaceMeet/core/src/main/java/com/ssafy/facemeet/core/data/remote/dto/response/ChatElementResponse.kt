package com.ssafy.facemeet.core.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class  ChatElementResponse(
    @SerializedName("content")
    val content: String?,
    @SerializedName("senderId")
    val senderID: Long?,
    @SerializedName("receiverId")
    val receiverID: Long?,
    @SerializedName("roomId")
    val roomID: Long?,
    @SerializedName("sendAt")
    val createdAt: String?,
    @SerializedName("isRead")
    val isRead: Boolean?,
    @SerializedName("readAt")
    val readAt: String?
)
