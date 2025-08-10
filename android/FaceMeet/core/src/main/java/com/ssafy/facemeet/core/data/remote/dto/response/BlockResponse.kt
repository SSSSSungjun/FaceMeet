package com.ssafy.facemeet.core.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class BlockResponse(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("nickName")
    val nickName: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("birth")
    val birth: String,
    @SerializedName("isDeleted")
    val isDeleted: Boolean
)
