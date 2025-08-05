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

    private var totalPages: Int = -1
    private var isInitialLoad = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatMessageItem> {
        return try {
            val page = params.key

            Log.d("ChatPagingSource", "페이지 로딩 시작 - page: $page, isInitialLoad: $isInitialLoad, roomId: $roomId")

            // 🚀 핵심: 초기 로드 시 Last API 사용, 이후 Current API 사용
            val response = if (page == null && isInitialLoad) {
                // 초기 로드: Last API로 마지막 페이지 바로 가져오기
                Log.d("ChatPagingSource", "초기 로드: Last API 사용")
                chatRepository.getChattingMessagesLast(roomId, 30)
            } else {
                // 페이징: Current API 사용
                val actualPage = page ?: 0
                Log.d("ChatPagingSource", "페이징 로드: Current API 사용 - page: $actualPage")
                chatRepository.getChattingMessagesCurrent(roomId, 30, actualPage)
            }

            response.fold(
                onSuccess = { chattingAllResponse ->
                    Log.d("ChatPagingSource", "API 호출 성공")

                    if (totalPages == -1) {
                        totalPages = chattingAllResponse.messages.totalPages.toInt()
                        Log.d("ChatPagingSource", "전체 페이지 수 설정: $totalPages")
                    }

                    val currentPage = if (isInitialLoad) {
                        // 초기 로드 시 마지막 페이지로 설정
                        val lastPage = totalPages - 1
                        Log.d("ChatPagingSource", "초기 로드 완료 - 마지막 페이지: $lastPage")
                        isInitialLoad = false
                        lastPage
                    } else {
                        chattingAllResponse.messages.currentPage.toInt()
                    }

                    val rawMessages = chattingAllResponse.messages.messages
                    Log.d("ChatPagingSource", "원본 메시지 개수: ${rawMessages.size}")

                    val messages = rawMessages.map { chatElement ->
                        ChatMessageItem(
                            chatElement = chatElement,
                            messageType = MessageType.TEXT
                        )
                    }.reversed() // 최신 메시지가 위로 오도록

                    Log.d("ChatPagingSource", "변환된 메시지 개수: ${messages.size}")
                    Log.d("ChatPagingSource", "현재 페이지: $currentPage")
                    Log.d("ChatPagingSource", "전체 페이지: $totalPages")

                    val result = LoadResult.Page(
                        data = messages,
                        prevKey = if (currentPage >= totalPages - 1) null else currentPage + 1, // 더 최신 페이지로
                        nextKey = if (currentPage <= 0) null else currentPage - 1 // 더 과거 페이지로
                    )

                    Log.d("ChatPagingSource", "LoadResult.Page 생성 완료 - prevKey: ${if (currentPage >= totalPages - 1) null else currentPage + 1}, nextKey: ${if (currentPage <= 0) null else currentPage - 1}")
                    result
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
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            val refreshKey = anchorPage?.prevKey?.minus(1) ?: anchorPage?.nextKey?.plus(1)
            Log.d("ChatPagingSource", "refreshKey: $refreshKey")
            refreshKey
        }
    }
}