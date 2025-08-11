package com.ssafy.facemeet.core.data.socket

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class ChatWebSocketManager @Inject constructor() {

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    // 연결 상태 관리
    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    // 현재 연결 정보
    private var currentUserId: Long = 0
    private var currentRoomId: Long = 0
    private var isStompConnected = false

    // 콜백 함수들
    private var onNewMessageReceived: ((ChatMessageItem) -> Unit)? = null
    private var onStompConnected: (() -> Unit)? = null
    var onReadNotification: (() -> Unit)? = null

    // 새 메시지 콜백 설정
    fun setOnNewMessageCallback(callback: (ChatMessageItem) -> Unit) {
        onNewMessageReceived = callback
    }

    // STOMP 연결 완료 콜백 설정
    fun setOnStompConnectedCallback(callback: () -> Unit) {
        onStompConnected = callback
    }

    // WebSocket 연결 시작
    fun connect(userId: Long, token: String, roomId: Long = 0) {
        currentUserId = userId
        currentRoomId = roomId
        Log.d("WebSocket", "WebSocket 연결 시작 - userId: $userId")
        _connectionState.postValue(ConnectionState.CONNECTING)

        CoroutineScope(Dispatchers.IO).launch {
            tryConnection(token)
        }
    }

    // 실제 연결 시도
    private fun tryConnection(token: String) {
        val websocketUrl = "wss://i13d201.p.ssafy.io/api/v1/websocket?token=$token"

        val wsClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val request = Request.Builder().url(websocketUrl).build()

        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ WebSocket 연결 성공!")
                _connectionState.postValue(ConnectionState.CONNECTED)
                sendStompConnect()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "📨 메시지 수신: $text")
                handleStompMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "❌ 연결 실패: ${t.message}")
                _connectionState.postValue(ConnectionState.ERROR)
                reconnect(token)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "🔌 연결 종료: $code - $reason")
                _connectionState.postValue(ConnectionState.DISCONNECTED)
                isStompConnected = false
            }
        })
    }

    // STOMP CONNECT 프레임 전송
    private fun sendStompConnect() {
        val connectFrame = "CONNECT\naccept-version:1.0,1.1,2.0\nheart-beat:10000,10000\n\n\u0000"
        webSocket?.send(connectFrame)
        Log.d("WebSocket", "📤 STOMP CONNECT 전송")
    }

    // STOMP 메시지 처리
    private fun handleStompMessage(message: String) {
        when {
            message.startsWith("CONNECTED") -> {
                Log.d("WebSocket", "🎉 STOMP 연결 완료!")
                isStompConnected = true
                sendStompMessage("/pub/chat.connect", currentUserId.toString())
                subscribeToPrivateChannel("/sub/private/$currentUserId")
                subscribeToReadChannel("/user/queue/read-receipt")
                onStompConnected?.invoke()
            }

            message.startsWith("MESSAGE") -> {
                parseStompMessage(message)
            }

            message.startsWith("ERROR") -> {
                Log.e("WebSocket", "❌ STOMP 오류: $message")
                _connectionState.postValue(ConnectionState.ERROR)
            }
        }
    }

    // STOMP 메시지 전송
    private fun sendStompMessage(destination: String, body: String) {
        if (!isStompConnected) {
            Log.w("WebSocket", "STOMP 연결되지 않은 상태")
            return
        }

        val frame =
            "SEND\ndestination:$destination\ncontent-type:application/json\ncontent-length:${body.toByteArray().size}\n\n$body\u0000"
        webSocket?.send(frame)
        Log.d("WebSocket", "📤 메시지 전송: $destination")
    }

    // 개인 채널 구독
    private fun subscribeToPrivateChannel(destination: String) {
        val subscribeFrame = "SUBSCRIBE\nid:sub-$currentUserId\ndestination:$destination\n\n\u0000"
        webSocket?.send(subscribeFrame)
        Log.d("WebSocket", "📡 구독: $destination")
    }

    // 읽음 확인 채널 구독
    private fun subscribeToReadChannel(destination: String) {
        val subscribeId = "read-receipt-${System.currentTimeMillis()}"
        val subscribeMessage = "SUBSCRIBE\nid:$subscribeId\ndestination:$destination\n\n\u0000"
        val success = webSocket?.send(subscribeMessage) == true
        Log.d("WebSocket", "📖 읽음 확인 채널 구독: $destination - 성공: $success")
    }

    // 채팅 메시지 전송
    fun sendMessage(
        content: String,
        roomId: Long,
        senderId: Long,
        receiverId: Long,
        tempId: Long? = null
    ) {
        val messageRequest = mapOf(
            "roomId" to roomId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
        )
        sendStompMessage("/pub/chat.private", gson.toJson(messageRequest))
        Log.d("WebSocket", "메시지 전송 완료 - ${gson.toJson(messageRequest)}")
    }

    // 읽음 처리 전송
    fun markAsRead(roomId: Long, userId: Long, senderId: Long) {
        val readRequest = mapOf(
            "readerId" to userId,
            "senderId" to senderId,
            "roomId" to roomId
        )
        Log.d("WebSocket", "읽음 처리 전송: ${gson.toJson(readRequest)}")
        sendStompMessage("/pub/chat.read", gson.toJson(readRequest))
    }

    // STOMP 메시지 파싱
    private fun parseStompMessage(message: String) {
        val lines = message.split("\n")
        val bodyStart = lines.indexOfFirst { it.isEmpty() }

        if (bodyStart != -1 && bodyStart + 1 < lines.size) {
            val body = lines.subList(bodyStart + 1, lines.size)
                .joinToString("\n")
                .replace("\u0000", "")

            Log.d("WebSocket", "수신된 메시지 바디: [$body]")

            val headers = lines.subList(1, bodyStart)
            val destination = headers.find { it.startsWith("destination:") }

            Log.d("WebSocket", "메시지 destination: $destination")

            try {
                if (body.startsWith("{")) {
                    if (destination?.contains("read-receipt") == true) {
                        Log.d("WebSocket", "📖 읽음 확인 수신: $body")
                        onReadNotification?.invoke()
                        return
                    }

                    val messageResponse = gson.fromJson(body, ChatElement::class.java)
                    Log.d("WebSocket", "💬 일반 메시지 파싱: $messageResponse")
                    handleChatMessage(messageResponse)
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "메시지 파싱 오류: ${e.message}")
            }
        }
    }

    var onNewMessageForList: (() -> Unit)? = null
    private fun handleChatMessage(response: ChatElement) {
        Log.d("WebSocket", "메시지 처리 시작 - 보낸사람: ${response.senderID}, 내용: ${response.content}")

        val messageItem = ChatMessageItem(
            chatElement = response,
            messageType = MessageType.TEXT
        )

        onNewMessageReceived?.invoke(messageItem)
        Log.d("WebSocket", "✅ 메시지 콜백 호출 완료")

        onNewMessageForList?.invoke()
    }

    // 재연결 시도
    private fun reconnect(token: String) {
        webSocket?.cancel()
        Log.d("WebSocket", "♻️ 3초 후 재연결 시도...")
        CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
            tryConnection(token)
        }
    }

    // 연결 종료
    fun disconnect() {
        Log.d("WebSocket", "연결 종료")

        if (isStompConnected) {
            val disconnectFrame = "DISCONNECT\n\n\u0000"
            webSocket?.send(disconnectFrame)
        }

        webSocket?.close(1000, "정상 종료")
        webSocket = null
        isStompConnected = false
        _connectionState.postValue(ConnectionState.DISCONNECTED)
    }
}