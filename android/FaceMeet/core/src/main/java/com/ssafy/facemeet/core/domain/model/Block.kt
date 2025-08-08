package com.ssafy.facemeet.core.domain.model

data class Block(
    val userId: Int,
    val nickName: String,
    val name: String,
    val gender: String,
    val birth: String,
    val isDeleted: Boolean
)