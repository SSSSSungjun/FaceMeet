package com.ssafy.facemeet.core.data.remote.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.datasource.ChatRemoteDataSource
import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.data.remote.mapper.toDomain
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.domain.model.ChattingAll
import com.ssafy.facemeet.core.domain.model.Matching
import com.ssafy.facemeet.core.domain.repository.ChatRepository
import javax.inject.Inject

private const val TAG = "ChatRepositoryImpl"

@RequiresApi(Build.VERSION_CODES.O)
class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource
) : ChatRepository {

    override suspend fun getChattingList(): Result<List<ChatListItem>> = runCatching {
        val response = remoteDataSource.getChattingList()
        if (response.isSuccessful) {
            Log.d(TAG, "getChattingList: ${response.body()}")
            (response.body() ?: emptyList()).map { it.toDomain() }
        } else {
            Log.d(TAG, "getChattingList: ${response.body()}")
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


    override suspend fun postChattingLike(roomId: Long, selected: Boolean): Result<Unit> =
        runCatching {
            val response = remoteDataSource.postChattingLike(roomId, selected)
            if (!response.isSuccessful) {
                throw Exception("Failed to like chat room: ${response.message()}")
            }
        }

    override suspend fun postChattingLeave(roomId: Long): Result<Unit> = runCatching {
        val response = remoteDataSource.postChattingLeave(roomId)
        if (!response.isSuccessful) {
            throw Exception("Failed to leave chat room: ${response.message()}")
        }
    }

    override suspend fun getChattingMessagesLast(roomId: Long, limit: Int): Result<ChattingAll> =
        runCatching {
            val response = remoteDataSource.getChattingMessagesLast(roomId, limit)
            Log.d(TAG, "getChattingMessagesLast: $response")
            if (response.isSuccessful) {
                Log.d(TAG, "getChattingMessagesLast: ${response.body()}")
                (response.body()?.toDomain()) ?: throw Exception("Empty response body")
            } else {
                Log.e("실패", "HTTP ${response.code()} - ${response.errorBody()?.string()}")
                throw Exception("Failed to fetch messages: ${response.message()}")
            }
        }

    override suspend fun getChattingMessagesCurrent(
        roomId: Long,
        limit: Int,
        page: Int
    ): Result<ChattingAll> =
        runCatching {
            val response = remoteDataSource.getChattingMessagesCurrent(roomId, limit, page)
            if (response.isSuccessful) {
                Log.d(TAG, "getChattingMessagesLast: ${response.body()}")
                (response.body()?.toDomain()) ?: throw Exception("Empty response body")
            } else {
                Log.e("실패", "HTTP ${response.code()} - ${response.errorBody()?.string()}")
                throw Exception("Failed to fetch messages: ${response.message()}")
            }
        }


}
