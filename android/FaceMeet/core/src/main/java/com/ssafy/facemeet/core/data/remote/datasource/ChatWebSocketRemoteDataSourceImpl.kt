package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.ChatWebSocketService
import com.ssafy.facemeet.core.data.remote.dto.request.ChatMessageRequest
import com.ssafy.facemeet.core.data.remote.dto.request.LeaveRoomRequest
import com.ssafy.facemeet.core.data.remote.dto.request.MarkAsReadRequest
import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ChatWebSocketRemoteDataSource @Inject constructor(
    private val chatWebSocketService: ChatWebSocketService
) {

    val events: Flow<WebSocketEvent> = chatWebSocketService.events

    suspend fun connect(userId: Long, token: String) {
        chatWebSocketService.connect(userId, token)
    }

    suspend fun disconnect() {
        chatWebSocketService.disconnect()
    }

    suspend fun sendMessage(request: ChatMessageRequest) {
        chatWebSocketService.sendMessage(request)
    }

    suspend fun markAsRead(request: MarkAsReadRequest) {
        chatWebSocketService.markAsRead(request)
    }

    suspend fun leaveRoom(request: LeaveRoomRequest) {
        chatWebSocketService.leaveRoom(request)
    }

    fun isConnected(): Boolean = chatWebSocketService.isConnected()
}