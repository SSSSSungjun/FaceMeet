package com.ssafy.facemeet.core.data.remote.mapper

import com.ssafy.facemeet.core.data.remote.dto.response.BlockResponse
import com.ssafy.facemeet.core.domain.model.Block

fun BlockResponse.toDomain(): Block {
    return Block(
        userId = this.userId,
        nickName = this.nickName,
        name = this.name,
        gender = this.gender,
        birth = this.birth,
        isDeleted = this.isDeleted
    )
}