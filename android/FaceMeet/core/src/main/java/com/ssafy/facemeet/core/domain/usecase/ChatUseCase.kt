package com.ssafy.facemeet.core.domain.usecase

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.data.repository.util.ChatPagingSource
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.domain.model.ChattingAll
import com.ssafy.facemeet.core.domain.model.Matching
import com.ssafy.facemeet.core.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
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

class GetChattingMessagesCurrentUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(roomId: Long): Flow<PagingData<ChatMessageItem>> {
        Log.d("GetChattingMessagesCurrentUseCase", "UseCase 호출 - roomId: $roomId")

        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                initialLoadSize = 30,
            ),
            pagingSourceFactory = {
                Log.d("GetChattingMessagesCurrentUseCase", "ChatPagingSource 생성 중...")
                ChatPagingSource(repository, roomId)
            }
        ).flow
    }
}