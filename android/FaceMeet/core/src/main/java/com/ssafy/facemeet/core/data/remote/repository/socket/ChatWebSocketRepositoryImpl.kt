package com.ssafy.facemeet.core.data.remote.repository.socket

import com.ssafy.facemeet.core.data.remote.datasource.ChatWebSocketRemoteDataSource
import com.ssafy.facemeet.core.data.remote.dto.request.ChatMessageRequest
import com.ssafy.facemeet.core.data.remote.dto.request.LeaveRoomRequest
import com.ssafy.facemeet.core.data.remote.dto.request.MarkAsReadRequest
import com.ssafy.facemeet.core.domain.repository.ChatWebSocketRepository
import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatWebSocketRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatWebSocketRemoteDataSource
) : ChatWebSocketRepository {
    
    override val events: Flow<WebSocketEvent> = remoteDataSource.events
    
    override fun connect(userId: Long, token: String) {

        CoroutineScope(Dispatchers.IO).launch {
            remoteDataSource.connect(userId, token)
        }
    }
    
    override fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            remoteDataSource.disconnect()
        }
    }
    
    override fun sendMessage(content: String, roomId: Long, senderId: Long, receiverId: Long) {
        val request = ChatMessageRequest(
            roomId = roomId,
            senderId = senderId,
            receiverId = receiverId,
            content = content
        )
        CoroutineScope(Dispatchers.IO).launch {
            remoteDataSource.sendMessage(request)
        }
    }
    
    override fun markAsRead(roomId: Long, userId: Long, senderId: Long) {
        val request = MarkAsReadRequest(
            readerId = userId,
            senderId = senderId,
            roomId = roomId
        )
        CoroutineScope(Dispatchers.IO).launch {
            remoteDataSource.markAsRead(request)
        }
    }
    
    override fun leaveRoom(userId: Long, partnerId: Long, roomId: Long) {
        val request = LeaveRoomRequest(
            userId = userId,
            partnerId = partnerId,
            roomId = roomId
        )
        CoroutineScope(Dispatchers.IO).launch {
            remoteDataSource.leaveRoom(request)
        }
    }
    
    fun isConnected(): Boolean = remoteDataSource.isConnected()
}