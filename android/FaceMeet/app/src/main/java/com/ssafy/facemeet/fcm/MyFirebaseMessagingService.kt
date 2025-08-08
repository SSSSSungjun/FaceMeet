package com.ssafy.facemeet.fcm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.facemeet.MainActivity
import com.ssafy.facemeet.core.data.database.NotificationDao
import com.ssafy.facemeet.core.data.database.entity.NotificationEntity
import com.ssafy.facemeet.core.data.remote.api.FcmService
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.FcmTokenResponse
import com.ssafy.facemeet.fcm.FcmAlarmHandler.triggerEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmService: FcmService

    @Inject
    lateinit var notificationDao: NotificationDao

    private val handler = Handler(Looper.getMainLooper())
    private val scheduledEvents = mutableMapOf<String, Runnable>()
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

        fcmService.registerDevice(request).enqueue(object : Callback<FcmTokenResponse?> {
            override fun onResponse(
                call: Call<FcmTokenResponse?>,
                response: Response<FcmTokenResponse?>
            ) {
                Log.d("FCM", "토큰 등록 성공: ${response.body()}")
            }

            override fun onFailure(call: Call<FcmTokenResponse?>, t: Throwable) {
                Log.e("FCM", "토큰 등록 실패", t)
            }
        })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "메시지 notification: ${remoteMessage.notification}")
        Log.d("FCM", "메시지 data: ${remoteMessage.data}")

        if (remoteMessage.data.isNotEmpty()) {
            when (remoteMessage.data["type"]) {
                "SCHEDULED_EVENT" -> handleScheduledEvent(
                    remoteMessage.notification,
                    remoteMessage.data
                )

                "IMMEDIATE_EVENT" -> handleImmediateEvent(
                    remoteMessage.notification,
                    remoteMessage.data
                )

                "PRE_MESSAGE" -> handleImmediateEvent(
                    remoteMessage.notification,
                    remoteMessage.data
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
        notification: RemoteMessage.Notification?,
        data: Map<String, String>
    ) {

        Log.d("FCM", "data: ${data.keys}")

        try {
            val settingId = data["settingId"] ?: return
            val triggerTimeStr = data["triggerTime"] ?: return
            val eventDataStr = data["eventData"] ?: return

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
                "FCM",
                "트리거 시간: ${
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerTime)
                }"
            )
            Log.d("FCM", "시간 차이 (분): ${(triggerTime.time - now.time) / (1000 * 60)}")

            // 1분 이상의 여유를 두고 판단 (네트워크 지연 등을 고려)
            val bufferTime = 60 * 1000L // 1분
            if (triggerTime.time < (now.time + bufferTime)) {
                Log.d("FCM", "즉시 실행: 트리거 시간이 현재 시간보다 이전이거나 1분 이내")
                triggerEvent(
                    context = this,
                    dao = notificationDao,
                    settingId = settingId,
                    eventDataStr = eventDataStr,
                    title = data["title"],
                    body = data["body"]
                )

            } else {
                Log.d("FCM", "예약 실행: 트리거 시간까지 대기")
                scheduleLocalEvent(
                    context = this, // `MyFirebaseMessagingService`는 Context 상속받음
                    settingId = settingId,
                    triggerTime = triggerTime,
                    eventDataStr = eventDataStr,
                    title = data["title"],
                    body = data["body"]
                )

            }
        } catch (e: Exception) {
            Log.e("FCM", "예약 이벤트 처리 오류", e)
        }
    }

    private fun handleImmediateEvent(
        notification: RemoteMessage.Notification?,
        data: Map<String, String>
    ) {
        Log.d("FCM", "handleImmediateEvent: ${data.entries}")
        val title = data["title"] ?: notification?.title ?: "없음"
        val body = data["body"] ?: notification?.body ?: "없음"
        sendNotification(title, body)
        saveNotificationToRoom(title, body, System.currentTimeMillis())
    }


    fun sendNotification(title: String, body: String) {
        val channelId = "ticket_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "이벤트 티켓 알림", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.ssafy.facemeet.client.R.drawable.logo_noti)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, builder.build())
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


    private fun storeEvent(
        settingId: String,
        triggerTime: Date,
        eventDataStr: String,
        title: String?,
        body: String?
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
                    json,
                    object : TypeToken<Map<String, String>>() {}.type
                )
                val settingId = eventInfo["settingId"] ?: continue
                val triggerTime =
                    eventInfo["triggerTime"]?.toLongOrNull()?.let { Date(it) } ?: continue
                val eventDataStr = eventInfo["eventData"] ?: continue
                val title = eventInfo["title"]
                val body = eventInfo["body"]

                if (triggerTime.time > System.currentTimeMillis()) {
                    scheduleLocalEvent(
                        context = this, // ✅ 요놈 추가하셈 돌대가리야
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


    private fun saveNotificationToRoom(title: String, body: String, triggerTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = NotificationEntity(
                title = title,
                body = body,
                triggerTime = triggerTime,
                type = "ticket"
            )
            notificationDao.insert(notification)
        }
    }

    private fun scheduleLocalEvent(
        context: Context,
        settingId: String,
        triggerTime: Date,
        eventDataStr: String,
        title: String?,
        body: String?
    ) {
        Log.d("FCM", "scheduleLocalEvent: title ${title} body: ${body}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, com.ssafy.facemeet.AlarmReceiver::class.java).apply {
            putExtra("settingId", settingId)
            putExtra("eventDataStr", eventDataStr)
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
                        AlarmManager.RTC_WAKEUP,
                        triggerTime.time,
                        pendingIntent
                    )
                } else {
                    Log.e("FCM", "정확한 알람 권한이 없어 예약 실패: SCHEDULE_EXACT_ALARM 필요")
                    // TODO: 유저에게 설정 권한 유도하거나 fallback 로직 넣기
                    return
                }
            } else {
                // Android 11 이하에서는 바로 호출 가능
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.time,
                    pendingIntent
                )
            }

            storeEvent(settingId, triggerTime, eventDataStr, title, body)
            Log.d("FCM", "알람 예약 성공: $settingId")
        } catch (e: SecurityException) {
            Log.e("FCM", "알람 예약 중 SecurityException 발생", e)
        } catch (e: Exception) {
            Log.e("FCM", "알람 예약 중 알 수 없는 예외 발생", e)
        }
    }


}


fun parseEventData(eventDataStr: String): Map<String, Any> {
    return try {
        Gson().fromJson(eventDataStr, object : TypeToken<Map<String, Any>>() {}.type)
    } catch (e: Exception) {
        Log.e("FCM", "이벤트 데이터 파싱 실패", e)
        emptyMap()
    }
}

object FcmAlarmHandler {

    fun triggerEvent(
        context: Context,
        dao: NotificationDao,
        settingId: String,
        eventDataStr: String,
        title: String?,
        body: String?
    ) {
        try {
            val finalTitle = title ?: "없음"
            val finalBody = body ?: "없음"

            sendNotification(context, finalTitle, finalBody)
            saveNotificationToRoom(dao, finalTitle, finalBody, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e("FCM", "외부 알람 실행 중 오류", e)
        }
    }

    private fun sendNotification(context: Context, title: String, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ticket_channel"

        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "티켓 알림", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.ssafy.facemeet.client.R.drawable.logo_noti)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun saveNotificationToRoom(
        dao: NotificationDao,
        title: String,
        body: String,
        time: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(
                NotificationEntity(
                    title = title,
                    body = body,
                    triggerTime = time,
                    type = "ticket"
                )
            )
        }
    }
}
