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
class SystemMessageManager @Inject constructor() {

    // 시스템 메시지 콜백
    var onSystemMessageAdded: ((ChatMessageItem) -> Unit)? = null

    private var dateTimer: Timer? = null
    private var midnightTimer: Timer? = null

    // 날짜 메시지
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



    fun startDateTimer(roomId: Long, currentUserId: Long) {
        stopDateTimer() // 기존 타이머 정리

        dateTimer = Timer()
        dateTimer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d("SystemMessage", "1분 타이머 - 날짜 메시지 추가")
                val dateMessage = createDateMessage(roomId, currentUserId)
                onSystemMessageAdded?.invoke(dateMessage)
            }
        }, 60000, 60000) // 1분 후 시작, 1분마다 반복
    }

    fun stopDateTimer() {
        dateTimer?.cancel()
        dateTimer = null
    }

    // 자정 타이머
    @SuppressLint("DiscouragedApi")
    fun startMidnightTimer(roomId: Long, currentUserId: Long) {
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
                Log.d("SystemMessage", "자정 - 날짜 메시지 추가")
                val dateMessage = createDateMessage(roomId, currentUserId)
                onSystemMessageAdded?.invoke(dateMessage)
            }
        }, calendar.time, 24 * 60 * 60 * 1000) // 24시간마다 반복
    }

    fun stopMidnightTimer() {
        midnightTimer?.cancel()
        midnightTimer = null
    }

    // 모든 타이머 정리
    fun stopAllTimers() {
        stopDateTimer()
        stopMidnightTimer()
    }
}