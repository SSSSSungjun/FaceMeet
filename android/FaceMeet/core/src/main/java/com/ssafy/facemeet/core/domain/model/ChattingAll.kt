package com.ssafy.facemeet.core.domain.model

data class ChattingAll(
    val messages: MutableList<ChatElement> = mutableListOf(),
    val totalPages: Int,
)