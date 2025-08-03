package com.ssafy.facemeet.core.domain.usecase

import android.util.Log
import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.domain.model.ChattingAll
import com.ssafy.facemeet.core.domain.model.Matching
import com.ssafy.facemeet.core.domain.repository.ChatRepository
import javax.inject.Inject

private const val TAG = "ChatUseCase"

class GetChattingListUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<ChatListItem>> {
//        return repository.getChattingList().map { list ->
//            list
//                .filter { !it.blocked } // 차단된 채팅 제외
//                .sortedWith(
//                    compareByDescending<ChatListItem> { it.lastSeen }
//                )
//        }
        Log.d(TAG, "invoke: 채팅리스트")
        return repository.getChattingList()
    }
}

class PostChattingListUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(request: MatchingUserRequest): Result<Matching> {
        return repository.postChattingList(request)
    }
}

class PostChattingLikeUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(roomId: Long, selected: Boolean): Result<Unit> {
        return repository.postChattingLike(roomId, selected)
    }
}

class PostChattingLeaveUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(roomId: Long): Result<Unit> {
        return repository.postChattingLeave(roomId)
    }
}

class GetChattingMessagesLastUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(roomId: Long, limit: Int): Result<ChattingAll> {
        return repository.getChattingMessagesLast(roomId, limit)
    }
}

class GetChattingMessagesAllUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(roomId: Long): Result<ChattingAll> {
        Log.d(TAG, "invoke: 불러오기 성공")
        return repository.getChattingMessagesAll(roomId)
    }
}

