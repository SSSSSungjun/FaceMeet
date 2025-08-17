package com.ssafy.facemeet.core.data.repository.util

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.repository.ChatRepository

class ChatPagingSource(
    private val chatRepository: ChatRepository,
    private val roomId: Long
) : PagingSource<Int, ChatMessageItem>() {

    override val jumpingSupported: Boolean = true
    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatMessageItem> {
        return try {
            val response = if (params.key == null) {
                Log.d("ChatPagingSource", "초기 로딩: Last API 사용")
                chatRepository.getChattingMessagesCurrent(roomId, 30, 0)
            } else {

                val page = params.key ?: 0
                Log.d("ChatPagingSource", "페이징 로딩: Current API 사용 - page: $page")
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

                    Log.d(
                        "ChatPagingSource",
                        "현재 페이지: $currentPage, 전체 페이지: $totalPages, size : ${messages.size}"
                    )

                    val prevKey = if (currentPage > 0) currentPage - 1 else null
                    val nextKey = if (currentPage < totalPages - 1) currentPage + 1 else null

                    LoadResult.Page(
                        data = messages,
                        prevKey = prevKey,
                        nextKey = nextKey // 더 최신 페이지
                    )
                },
                onFailure = { throwable ->
                    Log.e("ChatPagingSource", "API 호출 실패", throwable)
                    LoadResult.Error(throwable)
                }
            )
        } catch (e: Exception) {
            Log.e("ChatPagingSource", "예외 발생", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ChatMessageItem>): Int? {
        Log.d("ChatPagingSource", "getRefreshKey 호출")
        return 0
    }
}
