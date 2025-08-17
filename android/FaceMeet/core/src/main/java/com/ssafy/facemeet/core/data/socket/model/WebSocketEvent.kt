// WebSocketEvent.kt
package com.ssafy.facemeet.core.data.socket.model

sealed class WebSocketEvent {
    data class NewMessageForRoomEvent(val message: ChatMessageItem) : WebSocketEvent()
    object ChatListUpdateEvent : WebSocketEvent() // 채팅 목록 갱신 필요 알림
    data class ReadReceiptEvent(val roomId: Long, val readAt: String) : WebSocketEvent()
    //data class NotificationEvent(val notification: NotificationElement) : WebSocketEvent()
    object StompConnectedEvent : WebSocketEvent() // STOMP 연결 성공 이벤트
    object DisconnectedEvent : WebSocketEvent() // 연결 끊김 이벤트
    object ErrorEvent : WebSocketEvent() // 오류 이벤트
}