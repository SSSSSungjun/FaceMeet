package com.ssafy.facemeet.fcm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

//    @Inject
//    lateinit var fcmService: FcmService

    @Inject
    lateinit var registerDeviceUseCase: RegisterDeviceUseCase

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "onNewToken: $token")

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val request = FcmTokenRequest(token, "android", deviceId)

        GlobalScope.launch(Dispatchers.IO) { //임시
            registerDeviceUseCase.invoke(request)
                .onSuccess { fcmTokenResponse ->
                    Log.d("FCM", "FCM 토큰 등록 성공: $fcmTokenResponse")
                }
                .onFailure { t ->
                    Log.e("FCM", "FCM 토큰 등록 실패", t)
                }
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "메시지 notification: ${remoteMessage.notification}")
        Log.d("FCM", "메시지 data: ${remoteMessage.data}")

        Log.d("FCM", "type: ${remoteMessage.data["type"]}")

        if (remoteMessage.data.isNotEmpty()) {
            when (remoteMessage.data["type"]) {
                "PRE_MESSAGE" -> handlePreMessage(
                    remoteMessage.notification, remoteMessage.data
                )

                "SCHEDULED_EVENT" -> handleScheduledEvent(
                    remoteMessage.data
                )

                "CHAT" -> handleChatNotification(
                    remoteMessage.notification, remoteMessage.data
                )

                else -> {
                    val title = remoteMessage.data["title"] ?: "알림"
                    val body = remoteMessage.data["body"] ?: ""
                    sendNotification(title, body)
                }
            }
        } else {
            remoteMessage.notification?.let {
                sendNotification(it.title ?: "알림", it.body ?: "")
            }
        }
    }

    private fun handleScheduledEvent(
        data: Map<String, String>
    ) {
        Log.d("FCM", "handleScheduledEvent함수호출")
        Log.d("FCM", "data: ${data.entries}")

        try {
            val settingId = data["settingId"] ?: return
            val triggerTimeStr = data["triggerTime"] ?: return

            val triggerTime = parseDateTime(triggerTimeStr)
            val now = Date()

            if (triggerTime == null) {
                Log.e("FCM", "시간 파싱 실패: $triggerTimeStr")
                return
            }

            // 디버깅을 위한 로그 추가
            Log.d(
                "FCM",
                "현재 시간: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now)}"
            )
            Log.d(
                "FCM", "트리거 시간: ${
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerTime)
                }"
            )
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
                    context = this, // `MyFirebaseMessagingService`는 Context 상속받음
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

    // ✅ NEW: PRE_MESSAGE → 이벤트 페이지 진입
    private fun handlePreMessage(
        notification: RemoteMessage.Notification?, data: Map<String, String>
    ) {
        val title = data["title"] ?: notification?.title ?: "없음"
        val body = data["body"] ?: notification?.body ?: "없음"
        val settingId = data["settingId"]!!.toLong()   // 서버 계약상 항상 옴

        // ✅ 공용 함수 사용
        FcmAlarmHandler.sendEventNotification(this, title, body, settingId)
    }


    private fun handleChatNotification(
        notification: RemoteMessage.Notification?, data: Map<String, String>
    ) {
        val roomId = data["roomId"]?.toLongOrNull()
        val title = notification?.title ?: "채팅 알림"
        val body = notification?.body ?: ""

        Log.d("FCM", "handleChatNotification: $roomId")
        Log.d("FCM", "handleChatNotification: ${AppStateManager.getCurrentScreen()}")
        if (roomId != null && AppStateManager.getCurrentScreen() == "ChattingScreen" && AppStateManager.isInChatRoom(
                roomId
            )
        ) {
            Log.d("FCM", "현재 채팅방($roomId)에 있어서 알림 스킵")

            // 화면 갱신만
            sendBroadcast(Intent("ACTION_REFRESH_CHAT").apply {
                putExtra("roomId", roomId)
            })
            return
        }

        if (AppStateManager.getCurrentScreen() == "ChatListScreen") {
            Log.d("FCM", "✅ 채팅 리스트 화면 감지됨")
            Log.d("FCM", "📡 브로드캐스트 발송 중...")

            val intent = Intent("ACTION_REFRESH_CHAT_LIST")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            Log.d("FCM", "📡 브로드캐스트 발송 완료")
            return
        }

        // 채팅방으로 이동하는 알림 생성
        sendNotification(title, body, roomId)
    }


    fun sendNotification(title: String, body: String) {
        val channelId = "ticket_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("facemeet://app/main")).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "이벤트 티켓 알림",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val noti = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_small)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), noti)
    }


    private fun sendNotification(title: String, body: String, roomId: Long? = null) {
        val channelId = "ticket_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val uri = if (roomId != null)
            Uri.parse("facemeet://app/chat/$roomId")
        else
            Uri.parse("facemeet://app/main")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pending = PendingIntent.getActivity(
            this, (roomId ?: 0L).toInt(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "채팅 알림", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "채팅 메시지 알림"
                    enableLights(true); enableVibration(true); setShowBadge(true)
                }
            )
        }

        val noti = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_small)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pending)
            .build()

        nm.notify((roomId ?: System.currentTimeMillis()).toInt(), noti)
    }


    private fun parseDateTime(dateTimeStr: String): Date? {
        // ISO 8601 형식과 다양한 형식 지원
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // UTC 시간
            "yyyy-MM-dd'T'HH:mm:ss'Z'",      // UTC 시간
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",  // 타임존 포함
            "yyyy-MM-dd'T'HH:mm:ssXXX",      // 타임존 포함
            "yyyy-MM-dd'T'HH:mm:ss.SSS",     // 밀리초 포함
            "yyyy-MM-dd'T'HH:mm:ss",         // 기본 ISO
            "yyyy-MM-dd'T'HH:mm",            // 분까지만
            "yyyy-MM-dd HH:mm:ss",           // 공백으로 구분
            "yyyy-MM-dd HH:mm"               // 공백으로 구분, 분까지만
        )

        for (formatStr in formats) {
            try {
                val sdf = SimpleDateFormat(formatStr, Locale.getDefault())
                // UTC 시간인 경우 타임존 설정
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
                    // TODO: 유저에게 설정 권한 유도하거나 fallback 로직 넣기
                    return
                }
            } else {
                // Android 11 이하에서는 바로 호출 가능
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


object FcmAlarmHandler {
    // ✅ 공용: 이벤트 페이지로 진입하는 알림 (public)
    fun sendEventNotification(context: Context, title: String, body: String, settingId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ticket_channel"

        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("facemeet://app/ticket_event/$settingId")).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        val pending = PendingIntent.getActivity(
            context, settingId.toInt(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "이벤트 티켓 알림", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val noti = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_small)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pending)
            .build()

        nm.notify(settingId.toInt(), noti)
    }


    // ... (기존 triggerEvent는 아래처럼 이 공용 함수를 호출)
    fun triggerEvent(
        context: Context,
        settingId: Long,
        title: String?,
        body: String?
    ) {
        val finalTitle = title ?: "없음"
        val finalBody = body ?: "없음"

        // ✅ 공용 함수 호출
        sendEventNotification(context, finalTitle, finalBody, settingId)

    }


}

