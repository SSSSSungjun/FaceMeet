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
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toHourMinuteString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class ChatWebSocketManager @Inject constructor() {

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private val _messages = MutableLiveData<List<ChatMessageItem>>()
    val messages: LiveData<List<ChatMessageItem>> = _messages

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val messageList = mutableListOf<ChatMessageItem>()
    private var currentUserId: Long = 0
    private var isStompConnected = false

    fun setInitialMessages(initialMessages: List<ChatMessageItem>) {
        messageList.clear()
        messageList.addAll(initialMessages)
        _messages.postValue(messageList.toList())
        Log.d("WebSocket", "초기 메시지 ${initialMessages.size}개 설정 완료")
    }

    fun connect(userId: Long, token: String) {
        currentUserId = userId

        Log.d("WebSocket", "WebSocket 연결 시작 - userId: $userId")
        _connectionState.postValue(ConnectionState.CONNECTING)

        tryConnection(token)
    }

    private fun tryConnection(token: String) {
        val websocketUrl = "wss://i13d201.p.ssafy.io/api/v1/websocket?token=$token"

        val wsClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(websocketUrl)
            .build()

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
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "🔌 연결 종료: $code - $reason")
                _connectionState.postValue(ConnectionState.DISCONNECTED)
                isStompConnected = false
            }
        })
    }

    private fun addSystemMessage(content: String, roomId: Long) {
        val systemMessage = ChatElement(
            content = content,
            senderID = -1, // 시스템 메시지는 -1로 구분
            receiverID = currentUserId,
            roomID = roomId,
            createdAt = ZonedDateTime.now().toString().toHourMinuteString(),
            isRead = true, // 시스템 메시지는 항상 읽음 처리
            readAt = ZonedDateTime.now().toString()
        )

        val messageItem = ChatMessageItem(
            chatElement = systemMessage,
            messageType = MessageType.SYSTEM
        )

        messageList.add(messageItem)
        _messages.postValue(messageList.toList())
    }

    // 입장 메시지
    fun sendJoinMessage(roomId: Long, userName: String = "사용자") {
        addSystemMessage("$userName 님이 입장하셨습니다.", roomId)
    }

    // 퇴장 메시지
    fun sendLeaveMessage(roomId: Long, userName: String = "사용자") {
        addSystemMessage("$userName 님이 퇴장하셨습니다.", roomId)
    }

    // 자정 시간 알림 메시지
    fun sendMidnightMessage(roomId: Long) {
        val currentDate = ZonedDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        )
        addSystemMessage("날짜가 변경되었습니다. $currentDate", roomId)
    }

    // 날짜 구분자 추가
    private fun addDateSeparator(date: String) {
        val dateMessage = ChatElement(
            content = date,
            senderID = -2, // 날짜 구분자는 -2로 구분
            receiverID = currentUserId,
            roomID = 0,
            createdAt = date,
            isRead = true,
            readAt = ZonedDateTime.now().toString()
        )

        val messageItem = ChatMessageItem(
            chatElement = dateMessage,
            messageType = MessageType.SYSTEM
        )

        messageList.add(messageItem)
        _messages.postValue(messageList.toList())
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
                parseStompMessage(message)
            }

            message.startsWith("ERROR") -> {
                Log.e("WebSocket", "❌ STOMP 오류: $message")
                _connectionState.postValue(ConnectionState.ERROR)
            }
        }
    }

    private fun sendStompMessage(destination: String, body: String) {
        if (!isStompConnected) {
            Log.w("WebSocket", "STOMP 연결되지 않은 상태")
            return
        }

        val frame = "SEND\ndestination:$destination\ncontent-type:application/json\ncontent-length:${body.toByteArray().size}\n\n$body\u0000"
        webSocket?.send(frame)
        Log.d("WebSocket", "📤 메시지 전송: $destination")
    }

    private fun subscribeToPrivateChannel(destination: String) {
        val subscribeFrame = "SUBSCRIBE\nid:sub-$currentUserId\ndestination:$destination\n\n\u0000"
        webSocket?.send(subscribeFrame)
        Log.d("WebSocket", "📡 구독: $destination")
    }

    fun sendMessage(content: String, roomId: Long, senderId: Long, receiverId: Long) {
        // 1. 내 메시지를 즉시 UI에 추가
        addMyMessageToUI(content, senderId, receiverId, roomId)

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

            try {
                if (body.startsWith("{")) {
                    val messageResponse = gson.fromJson(body, ChatElement::class.java)
                    handleChatMessage(messageResponse)
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "메시지 파싱 오류: ${e.message}")
            }
        }
    }

    private fun addMyMessageToUI(content: String, senderId: Long, receiverId: Long, roomId: Long) {
        val myMessage = ChatElement(
            content = content,
            senderID = senderId,
            receiverID = receiverId,
            roomID = roomId,
            createdAt = ZonedDateTime.now().toString().toHourMinuteString(),
            isRead = false,
            readAt = ZonedDateTime.now().toString()
        )

        val messageItem = ChatMessageItem(
            chatElement = myMessage,
            messageType = MessageType.TEXT
        )

        messageList.add(messageItem)
        _messages.postValue(messageList.toList())
    }

    private fun handleChatMessage(response: ChatElement) {
        Log.d("WebSocket", "메시지 처리 - 보낸사람: ${response.senderID}")

        if (response.senderID != currentUserId) {
            Log.d("WebSocket", "상대방 메시지 추가")
            addMessageToList(response)
        } else {
            Log.d("WebSocket", "내 메시지 - 이미 UI에 있음")
        }
    }

    private fun addMessageToList(response: ChatElement) {
        val messageItem = ChatMessageItem(
            chatElement = response,
            messageType = MessageType.TEXT
        )
        messageList.add(messageItem)
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
        _connectionState.postValue(ConnectionState.DISCONNECTED)
    }

    fun clearMessages() {
        messageList.clear()
        _messages.postValue(emptyList())
    }
}