package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.data.remote.dto.request.MatchingUserRequest
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.domain.model.ChatListItem
import com.ssafy.facemeet.core.domain.model.Matching
import com.ssafy.facemeet.core.domain.repository.ChatRepository
import javax.inject.Inject

class GetChattingListUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<ChatListItem>> {
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
    suspend operator fun invoke(roomId: Int, selected: Boolean): Result<Unit> {
        return repository.postChattingLike(roomId, selected)
    }
}

class PostChattingLeaveUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(roomId: Int): Result<Unit> {
        return repository.postChattingLeave(roomId)
    }
}

class GetChattingMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(roomId: Int, limit: Int): Result<List<ChatElement>> {
        return repository.getChattingMessages(roomId, limit)
    }
}
