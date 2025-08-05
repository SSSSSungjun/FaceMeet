package com.ssafy.facemeet.core.data.remote.datasource

import android.util.Log
import com.ssafy.facemeet.core.data.remote.api.ChatApiService
import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.data.remote.dto.response.ChatListItemResponse
import com.ssafy.facemeet.core.data.remote.dto.response.ChattingAllResponse
import com.ssafy.facemeet.core.data.remote.dto.response.MatchingResponse
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "ChatRemoteDataSource"

class ChatRemoteDataSource @Inject constructor(
    private val chatApiService: ChatApiService
) {

    suspend fun getChattingList(): Response<MutableList<ChatListItemResponse>> {
        Log.d(TAG, "getChattingList: remote")
        return chatApiService.getChattingList()
    }

    suspend fun postChattingList(request: MatchingUserRequest): Response<MatchingResponse> {
        return chatApiService.postChattingList(request)
    }

    suspend fun postChattingLike(roomId: Long, selected: Boolean): Response<Unit> {
        return chatApiService.postChattingLike(roomId, selected)
    }

    suspend fun postChattingLeave(roomId: Long): Response<Unit> {
        return chatApiService.postChattingLeave(roomId)
    }

    suspend fun getChattingMessagesLast(roomId: Long, limit: Int): Response<ChattingAllResponse> {
        return chatApiService.getChattingMessagesLast(roomId, limit)
    }

    suspend fun getChattingMessagesCurrent(
        roomId: Long,
        limit: Int,
        page: Int
    ): Response<ChattingAllResponse> {
        return chatApiService.getChattingMessagesCurrent(roomId, limit, page)
    }
}
