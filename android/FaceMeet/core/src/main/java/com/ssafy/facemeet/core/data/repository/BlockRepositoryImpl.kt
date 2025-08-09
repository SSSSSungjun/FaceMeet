package com.ssafy.facemeet.core.data.repository// BlockRepositoryImpl.kt

import android.util.Log
import com.ssafy.facemeet.core.data.remote.datasource.BlockRemoteDataSource
import com.ssafy.facemeet.core.data.remote.mapper.toDomain
import com.ssafy.facemeet.core.domain.model.Block
import com.ssafy.facemeet.core.domain.repository.BlockRepository
import javax.inject.Inject

private const val TAG = "BlockRepositoryImpl"

class BlockRepositoryImpl @Inject constructor(
    private val remoteDataSource: BlockRemoteDataSource
) : BlockRepository {

    override suspend fun postBlockUser(userId: Long): Result<Unit> = runCatching {
        val response = remoteDataSource.postBlockUser(userId)
        if (response.isSuccessful) {
            Log.d(TAG, "getBlockList: ${response.body()}")
            response.body() // 성공 시 Unit 반환
        } else {
            throw Exception("Failed to block user: ${response.code()} - ${response.message()}")
        }
    }

    override suspend fun getBlockList(): Result<MutableList<Block>> = runCatching {
        val response = remoteDataSource.getBlockList()
        if (response.isSuccessful) {
            Log.d(TAG, "getBlockList: ${response.body()}")
            (response.body() ?: mutableListOf()).map { it.toDomain() }.toMutableList()
        } else {
            throw Exception("Failed to get block list: ${response.code()} - ${response.message()}")
        }
    }

    override suspend fun deleteBlockUser(blockedId: Long): Result<Unit> = runCatching {
        val response = remoteDataSource.deleteBlockUser(blockedId)
        if (response.isSuccessful) {
            response.body()
        } else {
            throw Exception("Failed to delete block: ${response.code()} - ${response.message()}")
        }
    }
}