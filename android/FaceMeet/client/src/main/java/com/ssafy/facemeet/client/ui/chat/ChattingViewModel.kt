package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ChatViewModel @Inject constructor(
    private val webSocketManager : ChatWebSocketManager
): ViewModel() {

    val messages = webSocketManager.messages
    val connectionState = webSocketManager.connectionState

    private var currentUserId: Long = 0
    private var currentRoomId: String = ""
    private var currentReceiverId: Long = 0

    fun initChat(roomId: String, userId: Long, receiverId: Long) {
        currentRoomId = roomId
        currentUserId = userId
        currentReceiverId = receiverId
        webSocketManager.connect(roomId, userId)
    }

    fun sendMessage(content: String) {
        if (content.isNotBlank()) {
            webSocketManager.sendMessage(
                content = content.trim(),
                roomId = currentRoomId,
                senderId = currentUserId,
                receiverId = currentReceiverId
            )
        }
    }

    fun markMessagesAsRead() {
        webSocketManager.markAsRead(currentRoomId, currentUserId)
    }

    fun endChat() {
        // 프론트에서 채팅 종료 처리
        webSocketManager.endChatRoom()
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}