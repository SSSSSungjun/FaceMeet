package com.ssafy.facemeet.core.domain.usecase

import android.util.Log
import com.ssafy.facemeet.core.domain.repository.ChatWebSocketRepository
import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectChatWebSocketUseCase @Inject constructor(
    private val repository: ChatWebSocketRepository
) {
    operator fun invoke(userId: Long, token: String) {
        repository.connect(userId, token)
    }
}

class DisconnectChatWebSocketUseCase @Inject constructor(
    private val repository: ChatWebSocketRepository
) {
    operator fun invoke() {
        repository.disconnect()
    }
}

class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatWebSocketRepository
) {
    operator fun invoke(content: String, roomId: Long, senderId: Long, receiverId: Long) {
        repository.sendMessage(content, roomId, senderId, receiverId)
    }
}

class MarkMessageAsReadUseCase @Inject constructor(
    private val repository: ChatWebSocketRepository
) {
    operator fun invoke(roomId: Long, userId: Long, senderId: Long) {
        repository.markAsRead(roomId, userId, senderId)
    }
}

class LeaveChatRoomUseCase @Inject constructor(
    private val repository: ChatWebSocketRepository
) {
    operator fun invoke(userId: Long, partnerId: Long, roomId: Long) {
        repository.leaveRoom(userId, partnerId, roomId)
    }
}

class ObserveChatEventsUseCase @Inject constructor(
    private val repository: ChatWebSocketRepository
) {
    operator fun invoke(): Flow<WebSocketEvent>{
        Log.d("ChatWebSocketUseCase", "invoke: ${repository.events}")
        return repository.events
    }
}
