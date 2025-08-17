package com.ssafy.facemeet.fcm

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.facemeet.R
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.domain.usecase.RegisterDeviceUseCase
import com.ssafy.facemeet.core.util.AppStateManager
import com.ssafy.facemeet.fcm.FcmAlarmHandler.triggerEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


private const val TAG = "FCM"

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var registerDeviceUseCase: RegisterDeviceUseCase

    @Inject
    lateinit var notificationDao: NotificationDao

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("scheduled_events", Context.MODE_PRIVATE)
        restoreScheduledEvents()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "onNewToken: $token")

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val request = FcmTokenRequest(token, "android", deviceId)

        GlobalScope.launch(Dispatchers.IO) {
            registerDeviceUseCase.invoke(request)
                .onSuccess {
                    Log.d("FCM", "FCM 토큰 등록 성공")
                }
                .onFailure { t ->
                    Log.e("FCM", "FCM 토큰 등록 실패", t)
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "메시지 notification: ${remoteMessage.notification}")
        Log.d("FCM", "메시지 data: ${remoteMessage.data}")

        val combinedData = remoteMessage.data.toMutableMap().apply {
            remoteMessage.notification?.let {
                put("title", it.title ?: "알림")
                put("body", it.body ?: "")
            }
        }

        if (combinedData.isNotEmpty()) when (combinedData["type"]) {
            "PRE_MESSAGE" -> handlePreMessage(combinedData)
            "SCHEDULED_EVENT" -> handleScheduledEvent(combinedData)
            "CHAT" -> handleChatNotification(combinedData)
            else -> {
                val title = combinedData["title"] ?: "알림"
                val body = combinedData["body"] ?: ""
                sendGenericNotification(title, body)
            }
        } else {
            remoteMessage.notification?.let {
                sendGenericNotification(it.title ?: "알림", it.body ?: "")
            }
        }
    }

    private fun handleScheduledEvent(data: Map<String, String>) {
        Log.d("FCM", "data: ${data.keys}")

        try {
            val settingId = data["settingId"] ?: return
            val triggerTimeStr = data["triggerTime"] ?: return

            val triggerTime = parseDateTime(triggerTimeStr)
            val now = Date()

            if (triggerTime == null) {
                Log.e("FCM", "시간 파싱 실패: $triggerTimeStr")
                return
            }

            Log.d("FCM", "현재 시간: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now)}")
            Log.d("FCM", "트리거 시간: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerTime)}")
            Log.d("FCM", "시간 차이 (분): ${(triggerTime.time - now.time) / (1000 * 60)}")

            val bufferTime = 60 * 1000L // 1분
            if (triggerTime.time < (now.time + bufferTime)) {
                Log.d("FCM", "즉시 실행: 트리거 시간이 현재 시간보다 이전이거나 1분 이내")
                triggerEvent(
                    context = this,
                    settingId = settingId.toLong(),
                    title = data["title"],
                    body = data["body"]
                )
            } else {
                Log.d("FCM", "예약 실행: 트리거 시간까지 대기")
                scheduleLocalEvent(
                    context = this,
                    settingId = settingId,
                    triggerTime = triggerTime,
                    title = data["title"] ?: "없음",
                    body = data["body"] ?: "없음"
                )
            }
        } catch (e: Exception) {
            Log.e("FCM", "예약 이벤트 처리 오류", e)
        }
    }

    private fun handlePreMessage(data: Map<String, String>) {
        val title = data["title"] ?: "없음"
        val body = data["body"] ?: "없음"
        val settingId = data["settingId"]!!.toLong()
        sendHeadsUpNotification(title, body, "ticket_event", settingId)
        saveNotificationToRoom(title, body, System.currentTimeMillis(), settingId = settingId)
    }

    private fun handleChatNotification(data: Map<String, String>) {
        val roomId = data["roomId"]?.toLongOrNull() ?: return
        val title = data["title"] ?: "채팅 알림"
        val body = data["body"] ?: ""

        val currentScreenInfo = AppStateManager.getCurrentScreen()
//        if (currentScreenInfo.first == "ChattingScreen" && currentScreenInfo.second == roomId) {
//            // 이미 해당 채팅방을 보고 있으므로 알림을 보내지 않음
//            Log.d("FCM", "사용자가 이미 채팅방에 있으므로 알림을 보내지 않음")
//            return
//        }
        // 알림 ID를 roomId로 고정하여 겹치지 않고 갱신되도록 함
        sendHeadsUpNotification(title, body, "chat", roomId)
        Log.d("FCM", "채팅 알림 전송: $title (Room ID: $roomId)")
    }

    private fun sendHeadsUpNotification(
        title: String,
        body: String,
        deepLink: String? = null,
        id: Long? = null
    ) {
        val channelId = when (deepLink) {
            "chat" -> "chat_channel_id"
            "ticket_event" -> "ticket_channel"
            else -> "general_channel"
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = when (deepLink) {
                "chat" -> "채팅 알림"
                "ticket_event" -> "이벤트 티켓 알림"
                else -> "일반 알림"
            }
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "백그라운드 헤드업 알림"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(false)
            }
            notificationManager.createNotificationChannel(channel)
            val createdChannel = notificationManager.getNotificationChannel(channelId)
            Log.d("FCM", "채널 [$channelName] 중요도: ${createdChannel?.importance}")
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            deepLink?.let { putExtra("deep_link", it) }
            Log.d(TAG, "deepLink: $deepLink")
            when (deepLink) {
                "chat" -> id?.let { putExtra("roomId", it) }
                "ticket_event" -> id?.let { putExtra("settingId", it) }
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            id?.toInt() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_small)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(when (deepLink) {
                "chat" -> NotificationCompat.CATEGORY_MESSAGE
                "ticket_event" -> NotificationCompat.CATEGORY_EVENT
                else -> NotificationCompat.CATEGORY_MESSAGE
            })
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setLights(Color.BLUE, 1000, 1000)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            builder.setFullScreenIntent(pendingIntent, true)
        } else {
            builder.setTimeoutAfter(15000)
        }
        try {
            val notification = builder.build()
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
            notificationManager.notify(
                id?.toInt() ?: 0,
                notification
            )
            Log.d("FCM", "헤드업 알림 전송 완료: $title")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNotifications = notificationManager.activeNotifications
                Log.d("FCM", "현재 활성 알림 개수: ${activeNotifications.size}")
            }
        } catch (e: Exception) {
            Log.e("FCM", "헤드업 알림 전송 실패", e)
        }
    }
    private fun sendGenericNotification(title: String, body: String) {
        sendHeadsUpNotification(title, body)
    }


    private fun parseDateTime(dateTimeStr: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )

        for (formatStr in formats) {
            try {
                val sdf = SimpleDateFormat(formatStr, Locale.getDefault())
                if (formatStr.contains("'Z'")) {
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val parsedDate = sdf.parse(dateTimeStr)
                Log.d("FCM", "시간 파싱 성공: $dateTimeStr -> $parsedDate (format: $formatStr)")
                return parsedDate
            } catch (e: Exception) {
                Log.v("FCM", "시간 파싱 시도 실패: $formatStr")
            }
        }
        Log.e("FCM", "모든 형식으로 시간 파싱 실패: $dateTimeStr")
        return null
    }

    private fun storeEvent(
        settingId: Long, triggerTime: Date, eventDataStr: String, title: String?, body: String?
    ) {
        val eventInfo = mapOf(
            "settingId" to settingId,
            "triggerTime" to triggerTime.time.toString(),
            "eventData" to eventDataStr,
            "title" to title.orEmpty(),
            "body" to body.orEmpty()
        )
        val json = Gson().toJson(eventInfo)
        sharedPreferences.edit().putString("event_$settingId", json).apply()
    }

    private fun removeStoredEvent(settingId: String) {
        sharedPreferences.edit().remove("event_$settingId").apply()
    }

    private fun restoreScheduledEvents() {
        try {
            val keys = sharedPreferences.all.keys.filter { it.startsWith("event_") }
            for (key in keys) {
                val json = sharedPreferences.getString(key, null) ?: continue
                val eventInfo = Gson().fromJson<Map<String, String>>(
                    json, object : TypeToken<Map<String, String>>() {}.type
                )
                val settingId = eventInfo["settingId"] ?: continue
                val triggerTime =
                    eventInfo["triggerTime"]?.toLongOrNull()?.let { Date(it) } ?: continue
                val eventDataStr = eventInfo["eventData"] ?: continue
                val title = eventInfo["title"] ?: "없음"
                val body = eventInfo["body"] ?: "없음"

                if (triggerTime.time > System.currentTimeMillis()) {
                    scheduleLocalEvent(
                        context = this,
                        settingId = settingId,
                        triggerTime = triggerTime,
                        eventDataStr = eventDataStr,
                        title = title,
                        body = body
                    )
                } else {
                    removeStoredEvent(settingId)
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "저장된 예약 복원 실패", e)
        }
    }

    private fun saveNotificationToRoom(
        title: String,
        body: String,
        triggerTime: Long,
        settingId: Long?
    ) {
        Log.d(TAG, "saveNotificationToRoom")
        CoroutineScope(Dispatchers.IO).launch {
            val notification = NotificationEntity(
                title = title,
                body = body,
                triggerTime = triggerTime,
                type = NotificationType.TICKET,
                settingId = settingId
            )
            notificationDao.insert(notification)
        }
    }

    private fun scheduleLocalEvent(
        context: Context,
        settingId: String,
        triggerTime: Date,
        title: String,
        body: String
    ) {
        Log.d("FCM", "scheduleLocalEvent: title ${title} body: ${body}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, com.ssafy.facemeet.AlarmReceiver::class.java).apply {
            putExtra("settingId", settingId)
            putExtra("title", title)
            putExtra("body", body)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            settingId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime.time, pendingIntent
                    )
                } else {
                    Log.e("FCM", "정확한 알람 권한이 없어 예약 실패: SCHEDULE_EXACT_ALARM 필요")
                    return
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime.time, pendingIntent
                )
            }

            Log.d("FCM", "알람 예약 성공: $settingId")
        } catch (e: SecurityException) {
            Log.e("FCM", "알람 예약 중 SecurityException 발생", e)
        } catch (e: Exception) {
            Log.e("FCM", "알람 예약 중 알 수 없는 예외 발생", e)
        }
    }
}

// 🔥 FcmAlarmHandler도 헤드업 알림으로 수정
object FcmAlarmHandler {

    fun sendEventNotification(context: Context, title: String, body: String, settingId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ticket_channel"

        // 🔥 헤드업을 위한 채널 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "이벤트 티켓 알림",
                NotificationManager.IMPORTANCE_HIGH // 헤드업을 위해 HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("deep_link", "ticket_event")
            putExtra("settingId", settingId)
        }
        val pending = PendingIntent.getActivity(
            context, settingId.toInt(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_small)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 헤드업을 위해 HIGH
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pending)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // 헤드업 확률 증가
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)

        // 🔥 Android 버전별 헤드업 설정
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            builder.setFullScreenIntent(pending, true)
        } else {
            builder.setTimeoutAfter(15000)
        }

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        nm.notify(settingId.toInt(), notification)
    }

    fun triggerEvent(
        context: Context,
        settingId: Long,
        title: String?,
        body: String?
    ) {
        val finalTitle = title ?: "없음"
        val finalBody = body ?: "없음"

        sendEventNotification(context, finalTitle, finalBody, settingId)

    }

    fun saveNotificationToRoom(
        dao: NotificationDao, title: String, body: String, time: Long, settingId: Long
    ) {
        Log.d(TAG, "saveNotificationToRoom")
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(
                NotificationEntity(
                    title = title,
                    body = body,
                    triggerTime = time,
                    type = NotificationType.TICKET,
                    settingId = settingId
                )
            )
        }
    }
}