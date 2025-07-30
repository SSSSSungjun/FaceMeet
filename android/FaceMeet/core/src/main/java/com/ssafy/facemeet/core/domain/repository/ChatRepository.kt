package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.domain.model.Matching

interface ChatRepository {
    suspend fun getChattingList(): Result<List<ChatListItem>>
    suspend fun postChattingList(request: MatchingUserRequest): Result<Matching>
    suspend fun postChattingLike(roomId: Int, selected: Boolean) :Result<Unit>
    suspend fun postChattingLeave(roomId: Int) : Result<Unit>
    suspend fun getChattingMessages(roomId: Int, limit: Int = 20): Result<List<ChatElement>>
}