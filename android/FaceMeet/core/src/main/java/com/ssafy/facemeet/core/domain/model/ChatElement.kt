package com.ssafy.facemeet.core.domain.model

import com.google.gson.annotations.SerializedName

data class ChatElement(
    val content: String,

    @SerializedName("senderId")
    val senderID: Long?,
    @SerializedName("receiverId")
    val receiverID: Long?,
    @SerializedName("roomId")
    val roomID: Long?,
    @SerializedName("sendAt")
    val createdAt: String,

    val isRead: Boolean,
    val readAt: String
){
    fun generateKey(): String = "${senderID}_${roomID}_${createdAt}_${content.hashCode()}"
}
