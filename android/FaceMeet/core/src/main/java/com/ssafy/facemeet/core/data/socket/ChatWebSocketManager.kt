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
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toFullDateString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class ChatWebSocketManager @Inject constructor() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    private val _messages = MutableLiveData<List<ChatMessageItem>>()
    val messages: LiveData<List<ChatMessageItem>> = _messages

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val messageList = mutableListOf<ChatMessageItem>()
    private var currentUserId: Long = 0
    private var isStompConnected = false

    fun connect(userId: Long, token: String) {
        currentUserId = userId
        Log.d("WebSocket", "순수 WebSocket + STOMP 연결 시도 - UserId: $userId")
        Log.d("WebSocket", "사용할 토큰: $token")

        // 토큰 만료 시간 확인
        checkTokenExpiry(token)

        _connectionState.value = ConnectionState.CONNECTING

        // JwtHandshakeInterceptor가 쿼리 파라미터에서 토큰을 추출하므로 쿼리 파라미터로만 시도
        connectWithQueryParam(token)
    }

    private fun checkTokenExpiry(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                Log.d("WebSocket", "토큰 payload: $payload")

                // exp 추출 (간단한 방법)
                val expMatch = Regex("\"exp\":(\\d+)").find(payload)
                if (expMatch != null) {
                    val expTime = expMatch.groupValues[1].toLong()
                    val currentTime = System.currentTimeMillis() / 1000
                    Log.d("WebSocket", "토큰 만료시간: $expTime")
                    Log.d("WebSocket", "현재 시간: $currentTime")
                    Log.d("WebSocket", "토큰 유효여부: ${expTime > currentTime}")

                    if (expTime <= currentTime) {
                        Log.e("WebSocket", "🚨 토큰이 만료되었습니다!")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "토큰 파싱 오류: ${e.message}")
        }
    }

    private fun connectWithQueryParam(token: String) {
        // URL 인코딩하지 않은 버전과 인코딩한 버전 둘 다 시도
        Log.d("WebSocket", "원본 토큰으로 시도...")
        tryConnection(token, "원본")
    }

    private fun tryConnection(token: String, type: String) {
        val websocketUrl = "wss://i13d201.p.ssafy.io/api/v1/websocket?token=$token"

        Log.d("WebSocket", "$type 토큰 방식: $websocketUrl")

        val request = Request.Builder()
            .url(websocketUrl)
            .addHeader("Origin", "https://i13d201.p.ssafy.io")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ $type 토큰으로 WebSocket 연결 성공!")
                _connectionState.postValue(ConnectionState.CONNECTED)
                sendStompConnect()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "📨 STOMP 메시지: $text")
                handleStompMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "❌ $type 토큰 WebSocket 연결 실패: ${t.message}")
                Log.e("WebSocket", "응답 코드: ${response?.code}")
                Log.e("WebSocket", "응답 메시지: ${response?.message}")

                when (response?.code) {
                    401 -> Log.e("WebSocket", "🔐 토큰 인증 실패")
                    400 -> Log.e("WebSocket", "📝 잘못된 요청 형식")
                    404 -> Log.e("WebSocket", "🔍 엔드포인트를 찾을 수 없음")
                    else -> Log.e("WebSocket", "❓ 알 수 없는 오류")
                }

                if (type == "원본" && response?.code == 401) {
                    Log.d("WebSocket", "URL 인코딩된 토큰으로 재시도...")
                    val encodedToken = java.net.URLEncoder.encode(token, "UTF-8")
                    tryConnection(encodedToken, "인코딩된")
                } else {
                    _connectionState.postValue(ConnectionState.ERROR)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "🔌 WebSocket 연결 종료: $code - $reason")
                _connectionState.postValue(ConnectionState.DISCONNECTED)
                isStompConnected = false
            }
        })
    }

    private fun sendStompConnect() {
        val connectFrame = "CONNECT\naccept-version:1.0,1.1,2.0\nheart-beat:10000,10000\n\n\u0000"
        webSocket?.send(connectFrame)
        Log.d("WebSocket", "📤 STOMP CONNECT 전송")
    }

    private fun handleStompMessage(message: String) {
        when {
            message.startsWith("CONNECTED") -> {
                Log.d("WebSocket", "🎉 STOMP 연결 완료!")
                isStompConnected = true

                // 서버 설정에 따라 /pub/chat.connect로 전송
                sendStompMessage("/pub/chat.connect", currentUserId.toString())

                // 개인 메시지 구독 (/sub/private/{userId})
                subscribeToPrivateChannel("/sub/private/$currentUserId")
            }

            message.startsWith("MESSAGE") -> {
                Log.d("WebSocket", "📬 메시지 수신")
                parseStompMessage(message)
            }

            message.startsWith("ERROR") -> {
                Log.e("WebSocket", "❌ STOMP 오류: $message")
                _connectionState.postValue(ConnectionState.ERROR)
            }

            else -> {
                Log.d("WebSocket", "🔄 기타 STOMP: $message")
            }
        }
    }

    private fun sendStompMessage(destination: String, body: String) {
        if (!isStompConnected) {
            Log.w("WebSocket", "STOMP가 연결되지 않은 상태")
            return
        }

        val frame = "SEND\ndestination:$destination\ncontent-type:application/json\ncontent-length:${body.toByteArray().size}\n\n$body\u0000"
        webSocket?.send(frame)
        Log.d("WebSocket", "📤 STOMP 메시지 전송: $destination -> $body")
    }

    private fun subscribeToPrivateChannel(destination: String) {
        val subscribeFrame = "SUBSCRIBE\nid:sub-$currentUserId\ndestination:$destination\n\n\u0000"
        webSocket?.send(subscribeFrame)
        Log.d("WebSocket", "📡 구독: $destination")
    }

    fun sendMessage(content: String, roomId: Int, senderId: Long, receiverId: Long) {
        val messageRequest = mapOf(
            "roomId" to roomId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content
        )

        // 서버 설정에 따라 /pub/chat.private로 전송
        sendStompMessage("/pub/chat.private", gson.toJson(messageRequest))
        addMyMessageToUI(content, roomId.toString(), senderId, receiverId)
    }

    fun markAsRead(roomId: String, userId: Long, senderId: Long) {
        val readRequest = mapOf(
            "readerId" to userId,
            "senderId" to senderId,
            "roomId" to roomId.toInt()
        )

        sendStompMessage("/pub/chat.read", gson.toJson(readRequest))
    }

    private fun parseStompMessage(message: String) {
        val lines = message.split("\n")
        val bodyStart = lines.indexOfFirst { it.isEmpty() }

        if (bodyStart != -1 && bodyStart + 1 < lines.size) {
            val body = lines.subList(bodyStart + 1, lines.size)
                .joinToString("\n")
                .replace("\u0000", "")

            Log.d("WebSocket", "메시지 본문: $body")

            try {
                if (body.startsWith("{")) {
                    val messageResponse = gson.fromJson(body, ChatElementResponse::class.java)
                    handleChatMessage(messageResponse)
                } else {
                    Log.d("WebSocket", "텍스트 메시지: $body")
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "메시지 파싱 오류: ${e.message}")
            }
        }
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
        val chatElement = ChatElementResponse(
            content = response.content ?: "",
            senderID = response.senderID ?: 0L,
            receiverID = response.receiverID ?: 0L,
            roomID = response.roomID?.toString() ?: "",
            createdAt = response.createdAt ?: ZonedDateTime.now().toString(),
            isRead = response.isRead ?: false,
            readAt = response.readAt ?: ""
        )

        if (chatElement.senderID != currentUserId) {
            addMessageToList(chatElement)
        }
    }

    private fun addMessageToList(response: ChatElementResponse) {
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

    fun endChatRoom() {
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

    fun disconnect() {
        Log.d("WebSocket", "연결 종료")

        if (isStompConnected) {
            val disconnectFrame = "DISCONNECT\n\n\u0000"
            webSocket?.send(disconnectFrame)
        }

        webSocket?.close(1000, "정상 종료")
        webSocket = null
        isStompConnected = false
        _connectionState.value = ConnectionState.DISCONNECTED

        messageList.clear()
        _messages.postValue(emptyList())
    }
}