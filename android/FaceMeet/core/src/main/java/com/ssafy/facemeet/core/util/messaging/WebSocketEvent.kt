package com.ssafy.facemeet.core.util.messaging

sealed class WebSocketEvent {
    data class ConnectionStateChanged(val state: ConnectionState) : WebSocketEvent()
    object StompConnected : WebSocketEvent()
    data class NewMessage(val messageItem: ChatMessageItem) : WebSocketEvent()
    data class UserLeft(val messageItem: ChatMessageItem) : WebSocketEvent()
    object ReadNotification : WebSocketEvent()
    data class MessageSentConfirmation(val content: String, val senderId: Long) : WebSocketEvent()
    data class WebSocketError(val throwable: Throwable, val responseCode: Int? = null, val reason: String? = null) : WebSocketEvent()
}

