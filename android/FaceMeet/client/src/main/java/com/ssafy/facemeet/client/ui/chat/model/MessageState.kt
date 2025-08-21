package com.ssafy.facemeet.client.ui.chat.model

import androidx.paging.PagingData
import com.ssafy.facemeet.core.util.messaging.ChatMessageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class MessageState(
    val pagedMessages: Flow<PagingData<ChatMessageItem>> = flowOf(PagingData.empty())
)

