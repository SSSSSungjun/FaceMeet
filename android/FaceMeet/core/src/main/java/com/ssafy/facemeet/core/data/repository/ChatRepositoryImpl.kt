package com.ssafy.facemeet.core.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.datasource.ChatRemoteDataSource
import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.data.remote.mapper.toDomain
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.domain.model.Matching
import com.ssafy.facemeet.core.domain.repository.ChatRepository
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource
) : ChatRepository {

    override suspend fun getChattingList(): Result<List<ChatListItem>> = runCatching {
        val response = remoteDataSource.getChattingList()
        if (response.isSuccessful) {
            (response.body() ?: emptyList()).map { it.toDomain() }
        } else {
            throw Exception("Failed to fetch chat list: ${response.message()}")
        }
    }

    override suspend fun postChattingList(request: MatchingUserRequest): Result<Matching> =
        runCatching {
            val response = remoteDataSource.postChattingList(request)
            if (response.isSuccessful) {
                response.body()?.toDomain() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to post chat list: ${response.message()}")
            }
        }

    override suspend fun postChattingLike(roomId: Int, selected: Boolean): Result<Unit> =
        runCatching {
            val response = remoteDataSource.postChattingLike(roomId, selected)
            if (!response.isSuccessful) {
                throw Exception("Failed to like chat room: ${response.message()}")
            }
        }

    override suspend fun postChattingLeave(roomId: Int): Result<Unit> = runCatching {
        val response = remoteDataSource.postChattingLeave(roomId)
        if (!response.isSuccessful) {
            throw Exception("Failed to leave chat room: ${response.message()}")
        }
    }

    override suspend fun getChattingMessages(roomId: Int, limit: Int): Result<List<ChatElement>> =
        runCatching {
            val response = remoteDataSource.getChattingMessages(roomId, limit)
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { it.toDomain() }
            } else {
                throw Exception("Failed to fetch messages: ${response.message()}")
            }
        }
}
