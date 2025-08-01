package com.ssafy.facemeet.core.data.remote.datasource

import android.util.Log
import com.ssafy.facemeet.core.data.remote.api.ChatApiService
import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.data.remote.dto.response.ChatElementResponse
import com.ssafy.facemeet.core.data.remote.dto.response.ChatListItemResponse
import com.ssafy.facemeet.core.data.remote.dto.response.MatchingResponse
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "ChatRemoteDataSource"
class ChatRemoteDataSource @Inject constructor(
    private val chatApiService: ChatApiService
)  {

     suspend fun getChattingList(): Response<MutableList<ChatListItemResponse>> {
         Log.d(TAG, "getChattingList: remote")
        return chatApiService.getChattingList()
    }

     suspend fun postChattingList(request: MatchingUserRequest): Response<MatchingResponse> {
        return chatApiService.postChattingList(request)
    }

     suspend fun postChattingLike(roomId: Int, selected: Boolean): Response<Unit> {
        return chatApiService.postChattingLike(roomId, selected)
    }

     suspend fun postChattingLeave(roomId: Int): Response<Unit> {
        return chatApiService.postChattingLeave(roomId)
    }

     suspend fun getChattingMessages(roomId: Int, limit: Int): Response<MutableList<ChatElementResponse>> {
        return chatApiService.getChattingMessages(roomId, limit)
    }
}
