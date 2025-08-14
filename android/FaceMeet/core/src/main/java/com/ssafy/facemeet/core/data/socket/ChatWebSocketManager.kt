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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class ChatWebSocketManager @Inject constructor() {

    private val messageQueue = ConcurrentLinkedQueue<String>()
    private val isProcessingQueue = AtomicBoolean(false)
    private val receiptIdCounter = AtomicLong(0)

    // Receipt 응답을 기다리는 메시지를 추적하는 맵
    private val awaitingReceipts = ConcurrentHashMap<String, String>()

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private var currentUserId: Long = 0
    private var currentRoomId: Long = 0
    private var currentPartnerId: Long = 0
    private var isStompConnected = false

    private var onNewMessageReceived: ((ChatMessageItem) -> Unit)? = null
    var onNewMessageLeaved: ((ChatMessageItem) -> Unit)? = null
    private var onStompConnected: (() -> Unit)? = null
    var onReadNotification: (() -> Unit)? = null
    var onNewMessageForList: (() -> Unit)? = null

    fun setOnNewMessageCallback(callback: (ChatMessageItem) -> Unit) {
        onNewMessageReceived = callback
    }

    fun setOnStompConnectedCallback(callback: () -> Unit) {
        onStompConnected = callback
    }

    fun connect(userId: Long, token: String, roomId: Long = 0, partnerId: Long = -1L) {
        currentUserId = userId
        currentRoomId = roomId
        if (partnerId != -1L) currentPartnerId = partnerId
        Log.d("WebSocket", "WebSocket 연결 시작 - userId: $userId")
        _connectionState.postValue(ConnectionState.CONNECTING)

        CoroutineScope(Dispatchers.IO).launch {
            tryConnection(token)
        }
    }

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
        Log.d("WebSocket", "처리할 메시지: $message")
        when {
            message.startsWith("CONNECTED") -> {
                Log.d("WebSocket", "🎉 STOMP 연결 완료!")
                isStompConnected = true
                enqueueStompMessage("/pub/chat.connect", currentUserId.toString())
                processQueue()
                subscribeToPrivateChannel("/sub/private/$currentUserId")
                onStompConnected?.invoke()
            }
            message.startsWith("MESSAGE") -> {
                parseStompMessage(message)
            }
            message.startsWith("RECEIPT") -> {
                handleReceipt(message)
            }
            message.startsWith("ERROR") -> {
                Log.e("WebSocket", "❌ STOMP 오류: $message")
            }
        }
    }

    private fun handleReceipt(message: String) {
        val receiptId = message.lines().find { it.startsWith("receipt-id:") }?.substringAfter("receipt-id:")?.trim()
        if (receiptId != null) {
            Log.d("WebSocket", "✅ 메시지 전송 확인 (Receipt): $receiptId")
            awaitingReceipts.remove(receiptId)
        }
    }

    private fun sendStompFrame(frame: String): Boolean {
        if (!isStompConnected) {
            Log.w("WebSocket", "STOMP 연결되지 않은 상태")
            return false
        }
        return webSocket?.send(frame) ?: false
    }

    private fun enqueueStompMessage(destination: String, body: String) {
        val receiptId = "receipt-${receiptIdCounter.incrementAndGet()}"
        val frame = buildStompFrame(destination, body, receiptId)
        messageQueue.offer(frame)
        processQueue()
    }

    private fun buildStompFrame(destination: String, body: String, receiptId: String? = null): String {
        val headers = mutableListOf("destination:$destination", "content-type:application/json")
        receiptId?.let { headers.add("receipt:$it") }
        val headerString = headers.joinToString("\n")
        return "SEND\n$headerString\ncontent-length:${body.toByteArray().size}\n\n$body\u0000"
    }

    private fun processQueue() {
        if (isProcessingQueue.compareAndSet(false, true)) {
            CoroutineScope(Dispatchers.IO).launch {
                while (messageQueue.isNotEmpty() || awaitingReceipts.isNotEmpty()) {
                    // 큐에서 다음 메시지 프레임을 가져옴
                    val frameToSend = messageQueue.peek()
                    if (frameToSend != null) {
                        val receiptId = frameToSend.lines().find { it.startsWith("receipt:") }?.substringAfter("receipt:")?.trim()

                        // 이미 전송하고 Receipt를 기다리는 중인지 확인
                        if (!awaitingReceipts.containsKey(receiptId)) {
                            val success = sendStompFrame(frameToSend)
                            if (success) {
                                if (receiptId != null) {
                                    awaitingReceipts[receiptId] = frameToSend
                                }
                                messageQueue.poll()
                            } else {
                                Log.e("WebSocket", "메시지 전송 실패. 재시도를 위해 큐에 유지합니다.")
                                delay(500)
                                isProcessingQueue.set(false)
                                break
                            }
                        }
                    }
                    delay(50)
                }
                isProcessingQueue.set(false)
            }
        }
    }

    private fun subscribeToPrivateChannel(destination: String) {
        val subscribeFrame = "SUBSCRIBE\nid:sub-$currentUserId\ndestination:$destination\n\n\u0000"
        webSocket?.send(subscribeFrame)
        Log.d("WebSocket", "📡 구독: $destination")
    }

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
        enqueueStompMessage("/pub/chat.private", gson.toJson(messageRequest))
        Log.d("WebSocket", "메시지 전송 요청 완료 (큐에 추가) - ${gson.toJson(messageRequest)}")
    }

    fun markAsRead(roomId: Long, userId: Long, senderId: Long) {
        val readRequest = mapOf(
            "readerId" to userId,
            "senderId" to senderId,
            "roomId" to roomId
        )
        enqueueStompMessage("/pub/chat.read", gson.toJson(readRequest))
        Log.d("WebSocket", "읽음 처리 요청 완료 (큐에 추가): ${gson.toJson(readRequest)}")
    }

    fun leaveRoom() {
        val leaveRequest = mapOf(
            "userId" to currentUserId,
            "partnerId" to currentPartnerId,
            "roomId" to currentRoomId
        )
        enqueueStompMessage("/pub/chat.leave", gson.toJson(leaveRequest))
        Log.d("WebSocket", "나가기 처리 요청 완료 (큐에 추가): ${gson.toJson(leaveRequest)}")
        //unsubscribe(currentUserId)
    }

    private fun parseStompMessage(message: String) {
        val lines = message.split("\n")
        val bodyStart = lines.indexOfFirst { it.isEmpty() }
        if (bodyStart != -1 && bodyStart + 1 < lines.size) {
            val body = lines.subList(bodyStart + 1, lines.size).joinToString("\n").replace("\u0000", "")
            Log.d("WebSocket", "수신된 메시지 바디: $body")

            try {
                if (body.startsWith("{")) {
                    val jsonObject = gson.fromJson(body, JsonObject::class.java)
                    val type = jsonObject.get("type")?.asString
                    when (type) {
                        "READ_RECEIPT" -> {
                            Log.d("WebSocket", "✅ 읽음 처리 성공 메시지 파싱: $body")
                            onReadNotification?.invoke()
                        }
                        "USER_LEFT" -> {
                            val leaveResponse = gson.fromJson(body, LeaveMessageResponse::class.java)
                            Log.d("WebSocket", "✅ 나가기 성공 메시지 파싱: $leaveResponse")
                            handleLeaveMessage(leaveResponse)
                        }
                        "LEAVE_ERROR" -> {
                            val leaveResponse = gson.fromJson(body, LeaveMessageResponse::class.java)
                            Log.d("WebSocket", "❌ 나가기 오류 메시지 파싱: $leaveResponse")
                        }
                        "MESSAGE" -> {
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

    private fun handleChatMessage(response: ChatElement) {
        Log.d("WebSocket", "메시지 처리 시작 - 보낸사람: ${response.senderID}, 내용: ${response.content}")
        val messageItem = ChatMessageItem(chatElement = response, messageType = MessageType.TEXT)
        onNewMessageReceived?.invoke(messageItem)
        Log.d("WebSocket", "✅ 메시지 콜백 호출 완료 $messageItem")
        onNewMessageForList?.invoke()
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
        val messageItem = ChatMessageItem(chatElement = tmpChatElement, messageType = MessageType.CHAT_END)
        onNewMessageLeaved?.invoke(messageItem)
    }

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