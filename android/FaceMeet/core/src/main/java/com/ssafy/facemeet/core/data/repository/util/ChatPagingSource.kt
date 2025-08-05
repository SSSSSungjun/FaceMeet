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

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatMessageItem> {
        return try {
            val response = if (params.key == null) {
                // 초기 로딩 시: Last API를 사용하여 마지막 페이지를 가져옵니다.
                // 이 로직은 앱 진입 시 한 번만 실행됩니다.
                Log.d("ChatPagingSource", "초기 로딩: Last API 사용")
                chatRepository.getChattingMessagesLast(roomId, 30)
            } else {
                // 추가 페이징 시: Paging 라이브러리에서 제공한 키(page)를 사용하여
                // 이전 페이지(더 오래된 메시지)를 가져옵니다.
                val page = params.key ?:0
                Log.d("ChatPagingSource", "페이징 로딩: Current API 사용 - page: $page")
                chatRepository.getChattingMessagesCurrent(roomId, 30, page)
            }

            response.fold(
                onSuccess = { chattingAllResponse ->
                    val rawMessages = chattingAllResponse.messages.messages

                    // reverseLayout=true이므로, 메시지를 오래된 순서로 정렬합니다.
                    // API 응답이 최신순이라면 여기서 뒤집어줘야 합니다.
                    val messages = rawMessages.map { chatElement ->
                        ChatMessageItem(
                            chatElement = chatElement,
                            messageType = MessageType.TEXT
                        )
                    }.reversed()

                    val currentPage = chattingAllResponse.messages.currentPage.toInt()
                    val totalPages = chattingAllResponse.messages.totalPages.toInt()

                    Log.d("ChatPagingSource", "현재 페이지: $currentPage, 전체 페이지: $totalPages")

                    // Paging 키를 올바르게 계산합니다.
                    // 위로 스크롤(더 오래된 메시지) -> prevKey 사용 -> 페이지 번호 증가
                    val prevKey = if (currentPage < totalPages - 1) currentPage + 1 else null

                    // 다음 페이지로 이동해야 하므로, nextKey는 페이지 번호가 더 작아야 합니다.
                    val nextKey = if (currentPage > 0) currentPage - 1 else null

                    LoadResult.Page(
                        data = messages,
                        prevKey = prevKey, // 더 오래된 페이지를 가리킵니다.
                        nextKey = nextKey // 더 최신 페이지를 가리킵니다.
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

    // getRefreshKey는 Paging이 화면을 갱신해야 할 때 호출됩니다.
    // 기존 로직을 단순화하여 앵커 위치에 해당하는 페이지의 키를 반환합니다.
    override fun getRefreshKey(state: PagingState<Int, ChatMessageItem>): Int? {
        Log.d("ChatPagingSource", "getRefreshKey 호출")
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }
}
