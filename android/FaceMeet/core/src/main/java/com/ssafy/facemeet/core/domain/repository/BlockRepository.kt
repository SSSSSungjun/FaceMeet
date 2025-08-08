package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.domain.model.Block

interface BlockRepository {
    suspend fun postBlockUser(userId: Long): Result<Unit>
    suspend fun getBlockList(): Result<MutableList<Block>>
    suspend fun deleteBlockUser(blockedId: Long): Result<Unit>
}