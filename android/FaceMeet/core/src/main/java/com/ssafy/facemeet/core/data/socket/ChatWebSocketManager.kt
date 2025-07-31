package com.ssafy.facemeet.core.data.socket

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ssafy.facemeet.core.data.remote.dto.response.ChatElementResponse
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.data.socket.model.WebSocketSendMessage
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toFullDateString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

const val SOCKET_URL = "ws://localhost:8080/ws"

@RequiresApi(Build.VERSION_CODES.O)
class ChatWebSocketManager @Inject constructor() {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    private val _messages = MutableLiveData<List<ChatMessageItem>>()
    val messages: LiveData<List<ChatMessageItem>> = _messages

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val messageList = mutableListOf<ChatMessageItem>()

    enum class ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED, ERROR
    }

    fun connect(roomId: String, userId: Long) {
        val request = Request.Builder()
            .url("ws://localhost:8080/ws")
            .build()

        _connectionState.value = ConnectionState.CONNECTING

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.postValue(ConnectionState.CONNECTED)
                Log.d("WebSocket", "연결 성공")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val response = gson.fromJson(text, ChatElementResponse::class.java)
                    handleIncomingMessage(response)
                } catch (e: Exception) {
                    Log.e("WebSocket", "메시지 파싱 오류: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.postValue(ConnectionState.DISCONNECTED)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.postValue(ConnectionState.ERROR)
                Log.e("WebSocket", "연결 오류: ${t.message}")
            }
        })
    }

    fun sendMessage(content: String, roomId: String, senderId: Long, receiverId: Long) {
        webSocket?.let { ws ->
            // 소켓 전송용 메시지
            val socketMessage = WebSocketSendMessage(
                roomId = roomId,
                senderId = senderId,
                receiverId = receiverId,
                content = content
            )
            ws.send(gson.toJson(socketMessage))

            // 내가 보낸 메시지는 즉시 UI에 추가 (임시 ChatElementResponse 생성)
            val tempResponse = ChatElementResponse(
                content = content,
                senderID = senderId,
                receiverID = receiverId,
                roomID = roomId,
                createdAt = ZonedDateTime.now().toString(),
                isRead = false,
                readAt = ""
            )
            addMessageToList(tempResponse)
        }
    }

    fun markAsRead(roomId: String, userId: Long) {
        webSocket?.let { ws ->
            // 읽음 처리용 특별한 소켓 메시지 (서버와 협의 필요)
            val readSocketMessage = mapOf(
                "type" to "read",
                "roomId" to roomId,
                "userId" to userId
            )
            ws.send(gson.toJson(readSocketMessage))
        }
    }

    private fun handleIncomingMessage(response: ChatElementResponse) {
        when {
            response.content == "READ_MESSAGE" -> {
                // 읽음 처리 - 기존 메시지들의 읽음 상태 업데이트
                updateMessagesReadStatus(response.roomID, response.senderID)
            }
            response.content == "CHAT_END" -> {
                addChatEndMessage()
            }
            else -> {
                addMessageToList(response)
            }
        }
    }

    private fun addMessageToList(response: ChatElementResponse) {
        // 날짜가 바뀌었는지 확인
        if (shouldAddDateSeparator(response.createdAt)) {
            addDateSeparator(response.createdAt)
        }

        val messageItem = ChatMessageItem(
            chatElement = response,
            messageType = MessageType.TEXT
        )
        messageList.add(messageItem)
        _messages.postValue(messageList.toList())
    }

    private fun shouldAddDateSeparator(createdAt: String): Boolean {
        if (messageList.isEmpty()) return true

        val lastMessage = messageList.lastOrNull { it.messageType == MessageType.TEXT }
            ?: return true

        try {
            val lastDate = ZonedDateTime.parse(lastMessage.chatElement.createdAt)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDate()

            val currentDate = ZonedDateTime.parse(createdAt)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDate()

            return lastDate != currentDate
        } catch (e: Exception) {
            return false
        }
    }

    private fun addDateSeparator(createdAt: String) {
        val dateResponse = ChatElementResponse(
            content = createdAt.toFullDateString(),
            senderID = -1,
            receiverID = -1,
            roomID = "",
            createdAt = createdAt,
            isRead = true,
            readAt = ""
        )

        val dateItem = ChatMessageItem(
            chatElement = dateResponse,
            messageType = MessageType.SYSTEM_DATE
        )
        messageList.add(dateItem)
    }

    private fun addChatEndMessage() {
        val endResponse = ChatElementResponse(
            content = "채팅방이 종료되었습니다",
            senderID = -1,
            receiverID = -1,
            roomID = "",
            createdAt = ZonedDateTime.now().toString(),
            isRead = true,
            readAt = ""
        )

        val endItem = ChatMessageItem(
            chatElement = endResponse,
            messageType = MessageType.CHAT_END
        )
        messageList.add(endItem)
        _messages.postValue(messageList.toList())
    }

    private fun updateMessagesReadStatus(roomId: String, userId: Long) {
        // 해당 사용자가 보낸 메시지들의 읽음 상태 업데이트
        var updated = false
        messageList.forEachIndexed { index, item ->
            if (item.chatElement.senderID == userId &&
                item.chatElement.roomID == roomId &&
                !item.chatElement.isRead) {

                val updatedResponse = item.chatElement.copy(
                    isRead = true,
                    readAt = ZonedDateTime.now().toString()
                )
                messageList[index] = item.copy(chatElement = updatedResponse)
                updated = true
            }
        }

        if (updated) {
            _messages.postValue(messageList.toList())
        }
    }

    fun endChatRoom() {
        // 채팅 종료 메시지를 프론트에서 직접 추가
        addChatEndMessage()
    }

    fun disconnect() {
        webSocket?.close(1000, "정상 종료")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
