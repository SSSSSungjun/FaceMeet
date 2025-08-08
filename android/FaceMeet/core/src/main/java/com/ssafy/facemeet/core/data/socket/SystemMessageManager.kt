package com.ssafy.facemeet.core.data.socket

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toHourMinuteString
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class SystemMessageManager @Inject constructor(
    private val webSocketManager: ChatWebSocketManager
) {
    var onSystemMessageAdded: ((ChatMessageItem) -> Unit)? = null

    private var dateTimer: Timer? = null
    private var midnightTimer: Timer? = null
    private var testTimer: Timer? = null

    // 시스템 메시지를 WebSocket으로 전송
    private fun sendSystemMessageToWebSocket(roomId: Long, currentUserId: Long, partnerId: Long, content: String) {
        webSocketManager.sendMessage(
            content = content,
            roomId = roomId,
            senderId = -2, // 시스템 메시지용 특별한 ID
            receiverId = partnerId,
            tempId = System.currentTimeMillis() // 임시 ID로 현재 시간 사용
        )
        Log.d("SystemMessage", "시스템 메시지 WebSocket 전송: $content")
    }

    // 날짜 메시지 생성 (로컬 표시용)
    fun createDateMessage(roomId: Long, currentUserId: Long): ChatMessageItem {
        val currentDate = ZonedDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        )

        val dateMessage = ChatElement(
            content = currentDate,
            senderID = -2,
            receiverID = currentUserId,
            roomID = roomId,
            createdAt = ZonedDateTime.now().toString().toHourMinuteString(),
            isRead = true,
            readAt = ZonedDateTime.now().toString()
        )

        return ChatMessageItem(
            chatElement = dateMessage,
            messageType = MessageType.DATE
        )
    }

    // 1분마다 날짜 메시지 전송
    fun startDateTimer(roomId: Long, currentUserId: Long, partnerId: Long) {
        stopDateTimer() // 기존 타이머 정리

        dateTimer = Timer()
        dateTimer?.schedule(object : TimerTask() {
            override fun run() {
                val currentDate = ZonedDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")
                )

                Log.d("SystemMessage", "1분 타이머 - 날짜 메시지 전송")

                // WebSocket으로 전송
                sendSystemMessageToWebSocket(roomId, currentUserId, partnerId, "📅 $currentDate")

                // 로컬 콜백도 호출 (필요한 경우)
                val dateMessage = createDateMessage(roomId, currentUserId)
                onSystemMessageAdded?.invoke(dateMessage)
            }
        }, 60000, 60000) // 1분 후 시작, 1분마다 반복

        Log.d("SystemMessage", "1분 날짜 타이머 시작됨")
    }

    // 자정 타이머 (실제 자정에 날짜 메시지 전송)
    @SuppressLint("DiscouragedApi")
    fun startMidnightTimer(roomId: Long, currentUserId: Long, partnerId: Long) {
        stopMidnightTimer() // 기존 타이머 정리

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        midnightTimer = Timer()
        midnightTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val newDate = ZonedDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
                )

                Log.d("SystemMessage", "자정 - 새로운 날짜 메시지 전송")

                // WebSocket으로 전송
                sendSystemMessageToWebSocket(roomId, currentUserId, partnerId, "🌙 새로운 하루가 시작되었습니다! $newDate")

                // 로컬 콜백도 호출
                val dateMessage = createDateMessage(roomId, currentUserId)
                onSystemMessageAdded?.invoke(dateMessage)
            }
        }, calendar.time, 24 * 60 * 60 * 1000) // 24시간마다 반복

        val timeUntilMidnight = calendar.timeInMillis - System.currentTimeMillis()
        Log.d("SystemMessage", "자정 타이머 시작됨 - ${timeUntilMidnight / 1000}초 후 실행")
    }

    // 테스트용 타이머 (10초마다 전송)
    fun startTestTimer(roomId: Long, currentUserId: Long, partnerId: Long) {
        stopTestTimer() // 기존 타이머 정리

        testTimer = Timer()
        testTimer?.schedule(object : TimerTask() {
            private var count = 1

            override fun run() {
                val testMessage = "🧪 테스트 메시지 #$count - ${ZonedDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                )}"

                Log.d("SystemMessage", "테스트 타이머 - 메시지 #$count 전송")
                sendSystemMessageToWebSocket(roomId, currentUserId, partnerId, testMessage)
                count++
            }
        }, 10000, 600000) // 10초 후 시작, 10초마다 반복

        Log.d("SystemMessage", "테스트 타이머 시작됨 - 10초마다 실행")
    }

    // 환영 메시지 전송 (채팅방 첫 진입시)
    fun sendWelcomeMessage(roomId: Long, currentUserId: Long, partnerId: Long, partnerName: String) {
        val welcomeMessage = "🎉 $partnerName 님과의 채팅이 시작되었습니다!"

        Log.d("SystemMessage", "환영 메시지 전송")
        sendSystemMessageToWebSocket(roomId, currentUserId, partnerId, welcomeMessage)
    }

    // 특별한 시스템 메시지 전송 (이벤트 등)
    fun sendCustomSystemMessage(roomId: Long, currentUserId: Long, partnerId: Long, message: String) {
        val customMessage = "📢 $message"

        Log.d("SystemMessage", "커스텀 시스템 메시지 전송: $message")
        sendSystemMessageToWebSocket(roomId, currentUserId, partnerId, customMessage)
    }

    // 개별 타이머 정지
    fun stopDateTimer() {
        dateTimer?.cancel()
        dateTimer = null
        Log.d("SystemMessage", "1분 날짜 타이머 정지됨")
    }

    fun stopMidnightTimer() {
        midnightTimer?.cancel()
        midnightTimer = null
        Log.d("SystemMessage", "자정 타이머 정지됨")
    }

    fun stopTestTimer() {
        testTimer?.cancel()
        testTimer = null
        Log.d("SystemMessage", "테스트 타이머 정지됨")
    }

    // 모든 타이머 정리
    fun stopAllTimers() {
        stopDateTimer()
        stopMidnightTimer()
        stopTestTimer()
        Log.d("SystemMessage", "모든 타이머 정리됨")
    }

    // 타이머 상태 확인
    fun getTimerStatus(): String {
        val status = mutableListOf<String>()
        if (dateTimer != null) status.add("날짜타이머")
        if (midnightTimer != null) status.add("자정타이머")
        if (testTimer != null) status.add("테스트타이머")

        return if (status.isEmpty()) "실행중인 타이머 없음" else "실행중: ${status.joinToString(", ")}"
    }
}