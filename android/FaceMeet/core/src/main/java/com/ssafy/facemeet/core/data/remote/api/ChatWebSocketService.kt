package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.ChatMessageRequest
import com.ssafy.facemeet.core.data.remote.dto.request.LeaveRoomRequest
import com.ssafy.facemeet.core.data.remote.dto.request.MarkAsReadRequest
import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import kotlinx.coroutines.flow.Flow

interface ChatWebSocketService {
    val events: Flow<WebSocketEvent>
    suspend fun connect(userId: Long, token: String)
    suspend fun disconnect()
    suspend fun sendMessage(request: ChatMessageRequest)
    suspend fun markAsRead(request: MarkAsReadRequest)
    suspend fun leaveRoom(request: LeaveRoomRequest)
    fun isConnected(): Boolean
}