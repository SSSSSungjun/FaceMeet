package com.ssafy.facemeet.core.data.remote.repository.page

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ssafy.facemeet.core.domain.repository.ChatRepository
import com.ssafy.facemeet.core.util.messaging.ChatMessageItem
import com.ssafy.facemeet.core.util.messaging.MessageType

private const val TAG = "ChatPagingSource"

class ChatPagingSource(
    private val chatRepository: ChatRepository,
    private val roomId: Long
) : PagingSource<Int, ChatMessageItem>() {

    override val jumpingSupported: Boolean = true
    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatMessageItem> {
        return try {
            val response = if (params.key == null) {
                Log.d(TAG, "초기 로딩: Last API 사용")
                chatRepository.getChattingMessagesCurrent(roomId, 30, 0)
            } else {

                val page = params.key ?: 0
                Log.d(TAG, "페이징 로딩: Current API 사용 - page: $page")
                chatRepository.getChattingMessagesCurrent(roomId, params.loadSize, page)
            }

            response.fold(
                onSuccess = { chattingAllResponse ->
                    val rawMessages = chattingAllResponse.messages.messages
                    val messages = rawMessages.map { chatElement ->
                        ChatMessageItem(
                            chatElement = chatElement,
                            messageType = MessageType.TEXT
                        )
                    }.reversed()

                    val currentPage = chattingAllResponse.messages.currentPage.toInt()
                    val totalPages = chattingAllResponse.messages.totalPages.toInt()

                    Log.d(TAG, "현재 페이지: $currentPage, 전체 페이지: $totalPages, size : ${messages.size}")

                    val prevKey = if (currentPage > 0) currentPage - 1 else null
                    val nextKey = if (currentPage < totalPages - 1) currentPage + 1 else null

                    LoadResult.Page(
                        data = messages,
                        prevKey = prevKey,
                        nextKey = nextKey // 더 최신 페이지
                    )
                },
                onFailure = { throwable ->
                    Log.e(TAG, "API 호출 실패", throwable)
                    LoadResult.Error(throwable)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "예외 발생", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ChatMessageItem>): Int? {
        Log.d(TAG, "getRefreshKey 호출")
        return 0
    }
}
