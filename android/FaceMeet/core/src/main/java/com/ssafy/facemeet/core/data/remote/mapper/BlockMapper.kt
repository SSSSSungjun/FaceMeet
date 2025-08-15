package com.ssafy.facemeet.core.data.remote.mapper

import com.ssafy.facemeet.core.data.remote.dto.response.BlockResponse
import com.ssafy.facemeet.core.domain.model.Block

fun BlockResponse.toDomain(): Block {
    return Block(
        userId = this.userId,
        nickName = this.nickName ?: "알 수 없음", // null이면 기본값
        name = this.name,
        gender = this.gender,
        birth = this.birth,
        isDeleted = this.isDeleted,
        img = this.img
    )
}

