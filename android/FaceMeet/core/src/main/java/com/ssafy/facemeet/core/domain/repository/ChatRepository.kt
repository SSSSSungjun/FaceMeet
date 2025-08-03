package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.domain.model.ChattingAll
import com.ssafy.facemeet.core.domain.model.Matching

interface ChatRepository {
    suspend fun getChattingList(): Result<List<ChatListItem>>
    suspend fun postChattingList(request: MatchingUserRequest): Result<Matching>
    suspend fun postChattingLike(roomId: Long, selected: Boolean) :Result<Unit>
    suspend fun postChattingLeave(roomId: Long) : Result<Unit>
    suspend fun getChattingMessagesLast(roomId: Long, limit: Int = 20): Result<ChattingAll>

    suspend fun getChattingMessagesAll(roomId : Long) : Result<ChattingAll>
}