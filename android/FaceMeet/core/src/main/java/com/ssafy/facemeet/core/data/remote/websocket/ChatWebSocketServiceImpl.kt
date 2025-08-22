package com.ssafy.facemeet.core.data.remote.websocket

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ssafy.facemeet.core.data.remote.api.ChatWebSocketService
import com.ssafy.facemeet.core.data.remote.dto.request.ChatMessageRequest
import com.ssafy.facemeet.core.data.remote.dto.request.LeaveRoomRequest
import com.ssafy.facemeet.core.data.remote.dto.request.MarkAsReadRequest
import com.ssafy.facemeet.core.data.remote.dto.response.LeaveMessageResponse
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.util.messaging.ChatMessageItem
import com.ssafy.facemeet.core.util.messaging.ConnectionState
import com.ssafy.facemeet.core.util.messaging.MessageType
import com.ssafy.facemeet.core.util.messaging.WebSocketEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.pow

private const val TAG = "ChatWebSocketServiceImpl"

@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class ChatWebSocketServiceImpl @Inject constructor(
    private val gson: Gson,
    @Named("ws") private val wsClient: OkHttpClient
) : ChatWebSocketService {

    private val messageQueue = ConcurrentLinkedQueue<String>()
    private val isProcessingQueue = AtomicBoolean(false)
    private val receiptIdCounter = AtomicLong(0)
    private val awaitingReceipts = ConcurrentHashMap<String, String>()

    private var webSocket: WebSocket? = null
    private var currentUserId: Long = 0
    private var isStompConnected = false
    private var token: String = ""
    private var reconnectAttempt = 0
    private var isManualDisconnect = false

    private val _events = MutableSharedFlow<WebSocketEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 64
    )
    override val events: Flow<WebSocketEvent> = _events.asSharedFlow()

    override suspend fun connect(userId: Long, token: String) {
        this.token = token
        this.currentUserId = userId
        isManualDisconnect = false
        reconnectAttempt = 0

        Log.d(TAG, "WebSocket 연결 시작 - userId: $userId")
        _events.emit(WebSocketEvent.ConnectionStateChanged(ConnectionState.CONNECTING))
        tryConnection()
    }

    override suspend fun disconnect() {
        Log.d(TAG, "연결 종료 요청")
        isManualDisconnect = true
        if (isStompConnected) {
            val disconnectFrame = "DISCONNECT\n\n\u0000"
            webSocket?.send(disconnectFrame)
            Log.d(TAG, "disconnect 전송")
        }
        webSocket?.close(1000, "정상 종료")
        webSocket = null
        isStompConnected = false
        _events.emit(WebSocketEvent.ConnectionStateChanged(ConnectionState.DISCONNECTED))
    }

    override suspend fun sendMessage(request: ChatMessageRequest) {
        val messageMap = mapOf(
            "roomId" to request.roomId,
            "senderId" to request.senderId,
            "receiverId" to request.receiverId,
            "content" to request.content,
        )

        enqueueStompMessage(PUB_CHAT_PRIVATE, gson.toJson(messageMap))
        Log.d(TAG, "메시지 전송 요청 완료 (큐에 추가) - ${gson.toJson(messageMap)}")

        _events.emit(WebSocketEvent.ConnectionStateChanged(ConnectionState.CONNECTING))
        _events.emit(WebSocketEvent.MessageSentConfirmation(request.content, request.senderId))
        if (!isStompConnected) {
            Log.w(TAG, "STOMP 연결이 끊어져 있습니다. 메시지 전송을 위해 즉시 재연결 시도.")
            reconnectAttempt = 0
            reconnect()
        }
    }

    override suspend fun markAsRead(request: MarkAsReadRequest) {
        val readMap = mapOf(
            "readerId" to request.readerId,
            "senderId" to request.senderId,
            "roomId" to request.roomId
        )
        enqueueStompMessage(PUB_CHAT_READ, gson.toJson(readMap))
        Log.d(TAG, "읽음 처리 요청 완료 (큐에 추가): ${gson.toJson(readMap)}")
    }

    override suspend fun leaveRoom(request: LeaveRoomRequest) {
        val leaveMap = mapOf(
            "userId" to request.userId,
            "partnerId" to request.partnerId,
            "roomId" to request.roomId
        )
        enqueueStompMessage(PUB_CHAT_LEAVE, gson.toJson(leaveMap))
        Log.d(TAG, "나가기 처리 요청 완료 (큐에 추가): ${gson.toJson(leaveMap)}")
    }

    override fun isConnected(): Boolean = isStompConnected

    private fun tryConnection() {
        if (isManualDisconnect) {
            Log.d(TAG, "수동 연결 해제 상태, 재연결 시도 중단")
            return
        }
        val websocketUrl = "wss://i13d201.p.ssafy.io/api/v1/websocket?token=$token"
        val request = Request.Builder().url(websocketUrl).build()

        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket 연결 성공!")
                CoroutineScope(Dispatchers.IO).launch {
                    _events.emit(WebSocketEvent.ConnectionStateChanged(ConnectionState.CONNECTED))
                    sendStompConnect()
                }
            }

            @SuppressLint("SuspiciousIndentation")
            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.isEmpty() || text == null) {
                    Log.d(TAG, "수신된 메시지는 $text")
                    return
                }
                Log.d(TAG, "수신된 메시지는 $text")
                handleStompMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "연결 실패: ${t.message}")
                CoroutineScope(Dispatchers.IO).launch {
                    _events.emit(WebSocketEvent.ConnectionStateChanged(ConnectionState.ERROR))
                    _events.emit(
                        WebSocketEvent.WebSocketError(
                            t,
                            response?.code,
                            response?.message
                        )
                    )
                }
                reconnect()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "연결 종료: $code - $reason")
                isStompConnected = false
                CoroutineScope(Dispatchers.IO).launch {
                    _events.emit(WebSocketEvent.ConnectionStateChanged(ConnectionState.DISCONNECTED))
                }
                if (!isManualDisconnect) {
                    reconnect()
                }
            }
        })
    }

    private fun reconnect() {
        if (isManualDisconnect) {
            Log.d(TAG, "수동 연결 해제 상태, 재연결 시도 중단")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            reconnectAttempt++
            val delayMillis = 1000L * 2.0.pow(reconnectAttempt.toDouble()).toLong()
            val maxDelay = 60000L
            val finalDelay = if (delayMillis > maxDelay) maxDelay else delayMillis

            Log.d(TAG, "재연결 시도 #$reconnectAttempt, ${finalDelay}ms 후 재시도")

            delay(finalDelay)
            if (isActive) {
                tryConnection()
            }
        }
    }

    private fun sendStompConnect() {
        val connectFrame = "CONNECT\naccept-version:1.0,1.1,2.0\nheart-beat:10000,10000\n\n\u0000"
        webSocket?.send(connectFrame)
        Log.d(TAG, "STOMP CONNECT frame: $connectFrame")
    }

    private fun handleStompMessage(message: String) {

        when {
            message.startsWith("CONNECTED") -> {
                Log.d(TAG, "STOMP 연결 완료!")
                isStompConnected = true
                enqueueStompMessage(PUB_CHAT_CONNECT, currentUserId.toString())
                processQueue()
                subscribeToPrivateChannel("$SUB_PRIVATE_USER/$currentUserId")
                CoroutineScope(Dispatchers.IO).launch {
                    _events.emit(WebSocketEvent.StompConnected)
                }
            }

            message.startsWith("MESSAGE") -> parseStompMessage(message)
            message.startsWith("RECEIPT") -> handleReceipt(message)
            message.startsWith("ERROR") -> {
                Log.e(TAG, "STOMP 오류: $message")
                CoroutineScope(Dispatchers.IO).launch {
                    _events.emit(WebSocketEvent.WebSocketError(Throwable("STOMP Error: $message")))
                }
            }
        }
    }

    private fun handleReceipt(message: String) {
        val receiptId = message.lines()
            .find { it.startsWith("receipt-id:") }
            ?.substringAfter("receipt-id:")
            ?.trim()
        if (receiptId != null) {
            Log.d(TAG, "메시지 전송 확인 (Receipt): $receiptId")
            awaitingReceipts.remove(receiptId)
        }
    }

    private fun sendStompFrame(frame: String): Boolean {
        if (!isStompConnected) {
            Log.w(TAG, "STOMP 연결되지 않은 상태")
            return false
        }
        return webSocket?.send(frame) == true
    }

    private fun enqueueStompMessage(destination: String, body: String) {
        val receiptId = "receipt-${receiptIdCounter.incrementAndGet()}"
        val frame = buildStompFrame(destination, body, receiptId)
        messageQueue.offer(frame)
        processQueue()
    }

    private fun buildStompFrame(
        destination: String,
        body: String,
        receiptId: String? = null
    ): String {
        val headers = mutableListOf("destination:$destination", "content-type:application/json")
        receiptId?.let { headers.add("receipt:$it") }
        val headerString = headers.joinToString("\n")
        return "SEND\n$headerString\ncontent-length:${body.toByteArray().size}\n\n$body\u0000"
    }

    private fun processQueue() {
        if (isProcessingQueue.compareAndSet(false, true)) {
            CoroutineScope(Dispatchers.IO).launch {
                while (messageQueue.isNotEmpty()) {
                    val frameToSend = messageQueue.peek()
                    if (frameToSend != null) {
                        val success = sendStompFrame(frameToSend)
                        if (success) {
                            messageQueue.poll()
                        } else {
                            Log.e(TAG, "메시지 전송 실패. 재시도를 위해 큐에 유지합니다.")
                            isProcessingQueue.set(false)
                            reconnect()
                            break
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
        Log.d(TAG, "구독: $subscribeFrame")
    }

    private fun parseStompMessage(message: String) {
        Log.d(TAG, "수신된 메시지 바디: $message")
        val lines = message.split("\n")
        val bodyStart = lines.indexOfFirst { it.isEmpty() }
        if (bodyStart != -1 && bodyStart + 1 < lines.size) {
            val body =
                lines.subList(bodyStart + 1, lines.size).joinToString("\n").replace("\u0000", "")
            Log.d(TAG, "수신된 메시지 바디: $body")
            try {
                if (body.startsWith("{")) {
                    val jsonObject = gson.fromJson(body, JsonObject::class.java)
                    val type = jsonObject.get("type")?.asString
                    when (type) {
                        "READ_RECEIPT" -> {
                            Log.d(TAG, "읽음 처리 성공 메시지 파싱")
                            CoroutineScope(Dispatchers.IO).launch {
                                _events.emit(WebSocketEvent.ReadNotification)
                            }
                        }

                        "USER_LEFT" -> {
                            val leaveResponse =
                                gson.fromJson(body, LeaveMessageResponse::class.java)
                            Log.d(TAG, "나가기 성공 메시지 파싱: $leaveResponse")
                            handleLeaveMessage(leaveResponse)
                        }

                        "LEAVE_ERROR" -> {
                            val leaveResponse =
                                gson.fromJson(body, LeaveMessageResponse::class.java)
                            Log.e(TAG, "나가기 오류 메시지 파싱: $leaveResponse")
                            CoroutineScope(Dispatchers.IO).launch {
                                _events.emit(WebSocketEvent.WebSocketError(Throwable("Leave Error: ${leaveResponse.message}")))
                            }
                        }

                        "MESSAGE" -> {
                            val messageResponse = gson.fromJson(body, ChatElement::class.java)
                            Log.d(TAG, "일반 메시지 파싱: $messageResponse")
                            handleChatMessage(messageResponse)
                            if (messageResponse.senderID == currentUserId) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    _events.emit(
                                        WebSocketEvent.MessageSentConfirmation(
                                            messageResponse.content.toString(),
                                            messageResponse.senderID
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "메시지 파싱 오류: ${e.message}", e)
                CoroutineScope(Dispatchers.IO).launch {
                    _events.emit(WebSocketEvent.WebSocketError(e))
                }
            }
        }
    }

    private fun handleChatMessage(response: ChatElement) {
        Log.d(TAG, "메시지 처리 시작 - 보낸 사람: ${response.senderID}, 내용: ${response.content}")
        val messageItem = ChatMessageItem(chatElement = response, messageType = MessageType.TEXT)
        CoroutineScope(Dispatchers.IO).launch {
            _events.emit(WebSocketEvent.NewMessage(messageItem))
            Log.d(TAG, "NewMessage 이벤트 발행 완료: $messageItem")
        }
    }

    private fun handleLeaveMessage(response: LeaveMessageResponse) {
        Log.d(TAG, "handleLeaveMessage: $response")
        val tmpChatElement = ChatElement(
            content = response.message,
            senderID = 0L,
            receiverID = 0L,
            roomID = response.roomId,
            createdAt = "",
            isRead = false,
            readAt = " ",
        )
        val messageItem =
            ChatMessageItem(chatElement = tmpChatElement, messageType = MessageType.CHAT_END)
        CoroutineScope(Dispatchers.IO).launch {
            _events.emit(WebSocketEvent.UserLeft(messageItem))
            Log.d(TAG, "UserLeft 이벤트 발행 완료: $messageItem")
        }
    }

    companion object {
        const val PUB_CHAT_CONNECT = "/pub/chat.connect"
        const val PUB_CHAT_PRIVATE = "/pub/chat.private"
        const val PUB_CHAT_READ = "/pub/chat.read"
        const val PUB_CHAT_LEAVE = "/pub/chat.leave"
        const val SUB_PRIVATE_USER = "/sub/private"
    }
}