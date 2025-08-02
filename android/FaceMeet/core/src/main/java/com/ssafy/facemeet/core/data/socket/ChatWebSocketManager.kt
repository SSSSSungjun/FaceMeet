package com.ssafy.facemeet.core.data.socket

import android.os.Build
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@RequiresApi(Build.VERSION_CODES.O)
class ChatWebSocketManager @Inject constructor() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val gson = Gson()

    private val _messages = MutableLiveData<List<ChatMessageItem>>()
    val messages: LiveData<List<ChatMessageItem>> = _messages

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val messageList = mutableListOf<ChatMessageItem>()
    private var currentUserId: Long = 0
    private var isStompConnected = false

    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3
    private var shouldReconnect = true
    private var connectionToken = ""
    private var isAppInForeground = true

    // 앱 상태 설정 메서드
    fun setAppForegroundState(isInForeground: Boolean) {
        isAppInForeground = isInForeground
        Log.d("WebSocket", "앱 상태 변경: ${if (isInForeground) "포그라운드" else "백그라운드"}")
    }

    fun connect(userId: Long, token: String) {
        connectionToken = token
        shouldReconnect = true
        reconnectAttempts = 0

        // 코루틴 스코프에서 TokenManager 호출
        CoroutineScope(Dispatchers.IO).launch {

            currentUserId = userId

            Log.d("WebSocket", "=== 연결 시작 ===")
            Log.d("WebSocket", "전달받은 userId: $userId")
            Log.d("WebSocket", "실제 사용할 currentUserId: $currentUserId")

            withContext(Dispatchers.Main) {
                checkTokenExpiry(token)
                _connectionState.value = ConnectionState.CONNECTING
                connectWithQueryParam(token)
            }
        }
    }

    private fun attemptReconnect() {
        if (!shouldReconnect || reconnectAttempts >= maxReconnectAttempts) {
            Log.d("WebSocket", "재연결 중단 - shouldReconnect: $shouldReconnect, attempts: $reconnectAttempts")
            _connectionState.value = ConnectionState.DISCONNECTED
            return
        }

        reconnectAttempts++
        val delay = (reconnectAttempts * 1500).toLong() // 1.5초, 3초, 4.5초

        Log.d("WebSocket", "재연결 시도 $reconnectAttempts/$maxReconnectAttempts (${delay}ms 후)")

        Handler(Looper.getMainLooper()).postDelayed({
            if (shouldReconnect) {
                Log.d("WebSocket", "실제 재연결 실행")
                connectWithQueryParam(connectionToken)
            }
        }, delay)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun checkTokenExpiry(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], android.util.Base64.DEFAULT))
                Log.d("WebSocket", "토큰 payload: $payload")

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
        Log.d("WebSocket", "원본 토큰으로 시도...")
        tryConnection(token, "원본")
    }

    private fun tryConnection(token: String, type: String) {
        val websocketUrl = "wss://i13d201.p.ssafy.io/api/v1/websocket?token=$token"

        Log.d("WebSocket", "$type 토큰 방식: $websocketUrl")

        val wsClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // 무한 대기
            .writeTimeout(15, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS) // ping 간격 줄임
            .retryOnConnectionFailure(true)
            .build()

        val request = Request.Builder()
            .url(websocketUrl)
            .header("Upgrade", "websocket")
            .header("Connection", "Upgrade")
            .header("Sec-WebSocket-Version", "13")
            .header("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==")
            .header("Origin", "https://i13d201.p.ssafy.io")
            .header("Host", "i13d201.p.ssafy.io")
            .header("User-Agent", "OkHttp WebSocket")
            .build()

        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ $type 토큰으로 WebSocket 연결 성공!")
                reconnectAttempts = 0
                _connectionState.postValue(ConnectionState.CONNECTED)
                sendStompConnect()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "📨 STOMP 메시지: $text")
                handleStompMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "❌ $type 토큰 WebSocket 연결 실패: ${t.message}")
                _connectionState.postValue(ConnectionState.ERROR)

                // 항상 재연결 시도 (화면 잠금 상관없이)
                if (shouldReconnect) {
                    attemptReconnect()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "🔌 WebSocket 연결 종료: $code - $reason")
                _connectionState.postValue(ConnectionState.DISCONNECTED)
                isStompConnected = false

                // 비정상 종료 시 항상 재연결 시도
                if (code != 1000 && shouldReconnect) {
                    Log.d("WebSocket", "비정상 종료 감지 - 재연결 시도")
                    attemptReconnect()
                }
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
                sendStompMessage("/pub/chat.connect", currentUserId.toString())
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
        // 1. 내 메시지를 즉시 UI에 추가 (빠른 반응성)
        addMyMessageToUI(content, roomId.toString(), senderId, receiverId)

        // 2. 서버로 메시지 전송
        val messageRequest = mapOf(
            "roomId" to roomId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content
        )
        sendStompMessage("/pub/chat.private", gson.toJson(messageRequest))
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
            roomID = roomId.toLong(),
            createdAt = ZonedDateTime.now().toString(),
            isRead = false,
            readAt = ZonedDateTime.now().toString(),
        )
        addMessageToList(tempResponse)
    }

    private fun handleChatMessage(response: ChatElementResponse) {
        Log.d("WebSocket", "=== 메시지 처리 시작 ===")
        Log.d("WebSocket", "handleChatMessage 원본: $response")
        Log.d("WebSocket", "TokenManager에서 가져온 현재 사용자 ID: $currentUserId")

        val chatElement = ChatElementResponse(
            content = response.content ?: "",
            senderID = response.senderID ?: 0L,
            receiverID = response.receiverID ?: 0L,
            roomID = response.roomID ?: 0,
            readAt = response.readAt ?: "",
            isRead = response.isRead ?: false,
            createdAt = response.createdAt ?: "",
        )

        Log.d("WebSocket", "파싱된 메시지 - 보낸사람: ${chatElement.senderID}, 받는사람: ${chatElement.receiverID}, 내용: ${chatElement.content}")
        Log.d("WebSocket", "비교: senderID(${chatElement.senderID}) == currentUserId($currentUserId) ? ${chatElement.senderID == currentUserId}")

        // 내가 보낸 메시지가 아닌 경우만 UI에 추가 (상대방 메시지만)
        if (chatElement.senderID != currentUserId) {
            Log.d("WebSocket", "✅ 상대방 메시지이므로 UI에 추가")
            addMessageToList(chatElement)
        } else {
            Log.d("WebSocket", "❌ 내가 보낸 메시지 - 이미 UI에 있으므로 스킵")
        }
        Log.d("WebSocket", "=== 메시지 처리 완료 ===")
    }

    private fun addMessageToList(response: ChatElementResponse) {
        if (shouldAddDateSeparator(response.readAt.toString())) {
            addDateSeparator(response.readAt.toString())
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
            val lastDate = ZonedDateTime.parse(lastMessage.chatElement.readAt)
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
            roomID = 0,
            createdAt = "",
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
            roomID = 0,
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
        Log.d("WebSocket", "연결 종료 요청")
        shouldReconnect = false

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