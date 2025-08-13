package com.ssafy.facemeet.core.data.socket

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.LeaveMessageResponse
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class ChatWebSocketManager @Inject constructor() {

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    // 연결 상태 관리
    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    // 현재 연결 정보
    private var currentUserId: Long = 0
    private var currentRoomId: Long = 0
    private var currentPartnerId: Long = 0
    private var isStompConnected = false

    // 콜백 함수들
    private var onNewMessageReceived: ((ChatMessageItem) -> Unit)? = null
    var onNewMessageLeaved: ((ChatMessageItem) -> Unit)? = null
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
    fun connect(userId: Long, token: String, roomId: Long = 0, partnerId: Long = -1L) {
        currentUserId = userId
        currentRoomId = roomId
        currentPartnerId=partnerId
        if (partnerId != -1L) currentPartnerId = partnerId
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
            .pingInterval(10, TimeUnit.SECONDS)
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
                reconnect(token)
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
        Log.d("WebSocket", "처리할 메시지: $message")
        when {
            message.startsWith("CONNECTED") -> {
                Log.d("WebSocket", "🎉 STOMP 연결 완료!")
                isStompConnected = true
                sendStompMessage("/pub/chat.connect", currentUserId.toString())
                subscribeToPrivateChannel("/sub/private/$currentUserId")
                onStompConnected?.invoke()
            }

            message.startsWith("MESSAGE") -> {
                parseStompMessage(message)
            }

            message.startsWith("ERROR") -> {
                Log.e("WebSocket", "❌ STOMP 오류: $message")
                //_connectionState.postValue(ConnectionState.ERROR)
                //reconnect(token)
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
        val success = webSocket?.send(subscribeFrame) == true
        Log.d("WebSocket", "📖 구독 성공: $destination - 성공: $success")
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

    fun leaveRoom() {
        Log.d("WebSocket", "나가기 처리 전송: $currentUserId")
        Log.d("WebSocket", "나가기 처리 전송: $currentPartnerId")
        Log.d("WebSocket", "나가기 처리 전송: $currentRoomId")

        val readRequest = mapOf(
            "userId" to currentUserId, //
            "partnerId" to currentPartnerId,
            "roomId" to currentRoomId
        )
        Log.d("WebSocket", "나가기 처리 전송: ${gson.toJson(readRequest)}")
        sendStompMessage("/pub/chat.leave", gson.toJson(readRequest))
        unsubscribe(currentUserId)
    }


    // STOMP 메시지 파싱
    private fun parseStompMessage(message: String) {
        val lines = message.split("\n")
        val bodyStart = lines.indexOfFirst { it.isEmpty() }

        if (bodyStart != -1 && bodyStart + 1 < lines.size) {
            val body = lines.subList(bodyStart + 1, lines.size)
                .joinToString("\n")
                .replace("\u0000", "")

            Log.d("WebSocket", "수신된 메시지 바디: $body")

            val headers = lines.subList(1, bodyStart)
            val destination = headers.find { it.startsWith("destination:") }

            Log.d("WebSocket", "메시지 destination: $destination")

            Log.d("WebSocket", "바디 첫 글자: '${body.firstOrNull()}'")
            Log.d("WebSocket", "바디 길이: ${body.length}")
            Log.d("WebSocket", "startsWith 체크: ${body.startsWith("{")}")


            try {
                if (body.startsWith("{")) {
                    val jsonObject = gson.fromJson(body, JsonObject::class.java)
                    val type = jsonObject.get("type")?.asString

                    when (type) {
                        "READ_RECEIPT"->{
                            Log.d("WebSocket", "✅ 읽음 처리 성공 메시지 파싱: $body")
                            onReadNotification?.invoke()
                            onReadNotification?.invoke()
                        }
                        "USER_LEFT" -> {
                            val leaveResponse =
                                gson.fromJson(body, LeaveMessageResponse::class.java)
                            Log.d("WebSocket", "✅ 나가기 성공 메시지 파싱: $leaveResponse")
                            handleLeaveMessage(leaveResponse)
                        }

                        "LEAVE_ERROR" -> {
                            val leaveResponse =
                                gson.fromJson(body, LeaveMessageResponse::class.java)
                            Log.d("WebSocket", "❌ 나가기 오류 메시지 파싱: $leaveResponse")

                        }

                        "MESSAGE" -> {
                            // 기존 일반 메시지 처리 로직
                            val messageResponse = gson.fromJson(body, ChatElement::class.java)
                            Log.d("WebSocket", "💬 일반 메시지 파싱: $messageResponse")
                            handleChatMessage(messageResponse)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "메시지 파싱 오류: ${e.message}")
            }
        }
    }

    var onNewMessageForList: (() -> Unit)? = null
        set(value) {
            field = value
            Log.d(
                "WebSocket",
                "onNewMessageForList 콜백 상태 변경: ${if (value != null) "설정됨" else "해제됨"}"
            )
        }

    private fun handleChatMessage(response: ChatElement) {
        Log.d("WebSocket", "메시지 처리 시작 - 보낸사람: ${response.senderID}, 내용: ${response.content}")

        val messageItem = ChatMessageItem(
            chatElement = response,
            messageType = MessageType.TEXT
        )

        onNewMessageReceived?.invoke(messageItem)
        Log.d("WebSocket", "✅ 메시지 콜백 호출 완료 $messageItem")

        Log.d("WebSocket", "onNewMessageForList 콜백 호출 직전")
        onNewMessageForList?.invoke()
        Log.d("WebSocket", "onNewMessageForList 콜백 호출 완료 여부: ${onNewMessageForList != null}")
    }


    private fun handleLeaveMessage(response: LeaveMessageResponse) {
        Log.d("WebSocket", "handleLeaveMessage: $response")
        val tmpChatElement = ChatElement(
            content = response.message,
            senderID = currentUserId,
            receiverID = currentPartnerId,
            roomID = response.roomId,
            createdAt = "",
            isRead = false,
            readAt = " ",
        )

        val messageItem = ChatMessageItem(
            chatElement = tmpChatElement,
            messageType = MessageType.CHAT_END
        )
        Log.d("WebSocket", "onNewMessageForLeave 콜백 호출 직전")
        onNewMessageLeaved?.invoke(messageItem)
        Log.d("WebSocket", "onNewMessageForLeave 콜백 호출 완료 여부: ${onNewMessageLeaved != null}")
    }


    // 재연결 시도
    private fun reconnect(token: String) {
        webSocket?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            tryConnection(token)
        }
    }

    fun unsubscribe(subscriptionId: Long) {
        if (isStompConnected) {
            val unsubscribeFrame = "UNSUBSCRIBE\nid:$subscriptionId\n\n\u0000"
            webSocket?.send(unsubscribeFrame)
            Log.d("WebSocket", "구독 취소: $subscriptionId")
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