package com.ssafy.facemeet.core.data.socket

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ssafy.facemeet.core.data.remote.dto.response.ChatElementResponse
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
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
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class ChatWebSocketManager @Inject constructor() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()
    private val messageCounter = AtomicLong(0)

    private val _messages = MutableLiveData<List<ChatMessageItem>>()
    val messages: LiveData<List<ChatMessageItem>> = _messages

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val messageList = mutableListOf<ChatMessageItem>()
    private var currentUserId: Long = 0


    fun connect(userId: Long, token: String) {
        currentUserId = userId

        val request = Request.Builder()
            .url("wss://i13d201.p.ssafy.io/api/v1/ws?token=$token")
            .addHeader("Sec-WebSocket-Protocol", "v10.stomp, v11.stomp, v12.stomp")
            .build()

        _connectionState.value = ConnectionState.CONNECTING

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "연결 성공")

                // STOMP CONNECT 프레임 전송
                sendStompConnect()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "수신 메시지: $text")
                handleStompMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.postValue(ConnectionState.DISCONNECTED)
                Log.d("WebSocket", "연결 종료: $code - $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.postValue(ConnectionState.ERROR)
                Log.e("WebSocket", "연결 오류: ${t.message}")
            }
        })
    }

    private fun sendStompConnect() {
        // STOMP CONNECT 프레임 생성
        val connectFrame = buildString {
            appendLine("CONNECT")
            appendLine("accept-version:1.0,1.1,2.0")
            appendLine("heart-beat:10000,10000")
            appendLine()
            append('\u0000') // NULL 종료 문자
        }

        webSocket?.send(connectFrame)
        Log.d("WebSocket", "STOMP CONNECT 전송: $connectFrame")
    }

    private fun handleStompMessage(message: String) {
        val lines = message.split("\n")
        val command = lines.firstOrNull() ?: return

        when (command) {
            "CONNECTED" -> {
                _connectionState.postValue(ConnectionState.CONNECTED)
                Log.d("WebSocket", "STOMP 연결 완료")

                // 연결 완료 후 초기 설정
                sendConnectMessage(currentUserId)
                subscribeToPrivateChannel(currentUserId)
            }
            "MESSAGE" -> {
                handleStompMessageFrame(lines)
            }
            "ERROR" -> {
                _connectionState.postValue(ConnectionState.ERROR)
                Log.e("WebSocket", "STOMP 오류: $message")
            }
        }
    }

    private fun sendConnectMessage(userId: Long) {
        sendStompMessage("/pub/chat.connect", userId.toString())
    }

    private fun subscribeToPrivateChannel(userId: Long) {
        val subscribeFrame = buildString {
            appendLine("SUBSCRIBE")
            appendLine("id:sub-$userId")
            appendLine("destination:/sub/private/$userId")
            appendLine()
            append('\u0000')
        }

        webSocket?.send(subscribeFrame)
        Log.d("WebSocket", "구독 요청: /sub/private/$userId")
    }

    private fun sendStompMessage(destination: String, body: String) {
        val messageId = messageCounter.incrementAndGet()

        val sendFrame = buildString {
            appendLine("SEND")
            appendLine("destination:$destination")
            appendLine("content-type:text/plain")
            appendLine("content-length:${body.toByteArray().size}")
            appendLine()
            append(body)
            append('\u0000')
        }

        webSocket?.send(sendFrame)
        Log.d("WebSocket", "STOMP 메시지 전송: $destination -> $body")
    }

    private fun handleStompMessageFrame(lines: List<String>) {
        // MESSAGE 프레임에서 본문 추출
        val bodyStartIndex = lines.indexOfFirst { it.isEmpty() } + 1
        if (bodyStartIndex < lines.size) {
            val body = lines.subList(bodyStartIndex, lines.size)
                .joinToString("\n")
                .replace("\u0000", "") // NULL 문자 제거

            try {
                if (body.startsWith("{")) {
                    // JSON 메시지 파싱
                    val response = gson.fromJson(body, ChatElementResponse::class.java)
                    handleChatMessage(response)
                } else {
                    // 단순 텍스트 메시지
                    Log.d("WebSocket", "알림 메시지: $body")
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "메시지 파싱 오류: ${e.message}")
            }
        }
    }

    fun sendMessage(content: String, roomId: Int, senderId: Long, receiverId: Long) {
        val messagePayload = WebSocketSendMessage(
            roomId = roomId.toString(),
            senderId = senderId,
            receiverId = receiverId,
            content = content
        )

        // JSON으로 직렬화해서 STOMP 메시지로 전송
        sendStompMessage("/pub/chat.private", gson.toJson(messagePayload))

        // 내가 보낸 메시지는 즉시 UI에 추가
        addMyMessageToUI(content, roomId.toString(), senderId, receiverId)
    }

    private fun addMyMessageToUI(content: String, roomId: String, senderId: Long, receiverId: Long) {
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

    private fun handleChatMessage(response: ChatElementResponse) {
        when {
            response.content == "READ_MESSAGE" -> {
                updateMessagesReadStatus(response.roomID, response.senderID)
            }
            response.content == "CHAT_END" -> {
                addChatEndMessage()
            }
            else -> {
                // 내가 보낸 메시지가 아닌 경우만 추가 (중복 방지)
                if (response.senderID != currentUserId) {
                    addMessageToList(response)
                }
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

    fun markAsRead(roomId: String, userId: Long) {
        // 읽음 처리 메시지 전송 (서버와 협의 필요)
        val readMessage = mapOf(
            "type" to "read",
            "roomId" to roomId,
            "userId" to userId
        )
        sendStompMessage("/pub/chat.read", gson.toJson(readMessage))
    }

    fun endChatRoom() {
        addChatEndMessage()
    }

    fun disconnect() {
        // STOMP DISCONNECT 프레임 전송
        val disconnectFrame = buildString {
            appendLine("DISCONNECT")
            appendLine()
            append('\u0000')
        }

        webSocket?.send(disconnectFrame)

        // WebSocket 연결 종료
        webSocket?.close(1000, "정상 종료")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}