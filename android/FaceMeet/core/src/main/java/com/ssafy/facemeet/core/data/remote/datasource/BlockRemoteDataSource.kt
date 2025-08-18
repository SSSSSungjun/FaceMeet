package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.BlockApiService
import com.ssafy.facemeet.core.data.remote.dto.response.BlockResponse
import retrofit2.Response
import javax.inject.Inject

class BlockRemoteDataSource @Inject constructor(
    private val apiService: BlockApiService
) {
    suspend fun postBlockUser(userId: Long) : Response<Unit>{
        return apiService.postBlockUser(userId)
    }

    suspend fun getBlockList(): Response<MutableList<BlockResponse>> {
        return apiService.getBlockList()
    }

    suspend fun deleteBlockUser(blockedId: Long): Response<Unit> {
        return apiService.deleteBlockUser(blockedId)
    }
}