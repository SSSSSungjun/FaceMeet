package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import kotlinx.coroutines.flow.Flow

interface ChatWebSocketRepository{
    val events: Flow<WebSocketEvent>
    fun connect(userId: Long, token: String)
    fun disconnect()
    fun sendMessage(content: String, roomId: Long, senderId: Long, receiverId: Long)
    fun markAsRead(roomId: Long, userId: Long, senderId: Long)
    fun leaveRoom(userId: Long, roomId: Long, partnerId: Long)
}