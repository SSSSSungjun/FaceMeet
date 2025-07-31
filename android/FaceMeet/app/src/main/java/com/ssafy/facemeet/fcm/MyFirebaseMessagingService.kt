package com.ssafy.facemeet.fcm

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
        val request = FcmTokenRequest(
            deviceToken = token,
            deviceType = "android",
            deviceId = deviceId
        )

        fcmService.registerDevice(request).enqueue(object : Callback<FcmTokenResponse> {
            override fun onResponse(
                call: Call<FcmTokenResponse?>,
                response: Response<FcmTokenResponse?>
            ) {
                Log.d("FCM", "토큰 등록 성공: ${response.body()}")
            }

            override fun onFailure(
                call: Call<FcmTokenResponse?>,
                t: Throwable
            ) {
                Log.e("FCM", "토큰 등록 실패", t)
            }
        })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "메시지 notification: ${remoteMessage.notification}")
        Log.d("FCM", "메시지 data: ${remoteMessage.data}")

        Log.d("FCM", "onMessageReceived: $remoteMessage")

        remoteMessage.notification?.let {
            sendNotification(it.title ?: "알림", it.body ?: "")
            return
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "onMessageReceived: $remoteMessage.data[\"type\"]")

            when (remoteMessage.data["type"]) {
                "SCHEDULED_EVENT" -> handleScheduledEvent(remoteMessage.data)
                "IMMEDIATE_EVENT" -> handleImmediateEvent(remoteMessage.data)
                else -> {
                    val title = remoteMessage.data["title"] ?: "알림"
                    val body = remoteMessage.data["body"] ?: ""
                    sendNotification(title, body)
                }
            }
        }
    }

    private fun handleScheduledEvent(data: Map<String, String>) {
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

            if (triggerTime.time <= now.time) {
                triggerEvent(settingId, eventDataStr)
            } else {
                scheduleLocalEvent(settingId, triggerTime, eventDataStr)
            }
        } catch (e: Exception) {
            Log.e("FCM", "예약 이벤트 처리 오류", e)
        }
    }

    private fun handleImmediateEvent(data: Map<String, String>) {
        Log.d("FCM", "handleImmediateEvent: handleImmediateEvent")
        val title = data["title"] ?: "🔥 선착순 이벤트 시작!"
        val body = data["body"] ?: "지금 바로 참여하세요!"
        sendNotification(title, body)

        saveNotificationToRoom(title, body, System.currentTimeMillis()) // room에 저장
    }

    private fun scheduleLocalEvent(settingId: String, triggerTime: Date, eventDataStr: String) {
        val delay = triggerTime.time - System.currentTimeMillis()

        if (delay <= 0) {
            triggerEvent(settingId, eventDataStr)
            return
        }

        scheduledEvents[settingId]?.let { handler.removeCallbacks(it) }

        val runnable = Runnable {
            triggerEvent(settingId, eventDataStr)
            scheduledEvents.remove(settingId)
            removeStoredEvent(settingId)
        }

        handler.postDelayed(runnable, delay)
        scheduledEvents[settingId] = runnable
        storeEvent(settingId, triggerTime, eventDataStr)

        Log.d("FCM", "예약됨: $settingId at $triggerTime (delay: ${delay}ms)")
    }

    private fun triggerEvent(settingId: String, eventDataStr: String) {
        Log.d("FCM", "🔥 이벤트 실행됨: $settingId")

        try {
            val eventData = parseEventData(eventDataStr)
            val couponCount = eventData["couponCount"] as? Double ?: 0.0
            val discountRate = eventData["discountRate"] as? Double ?: 0.0

            val title = "🔥 선착순 이벤트 시작!"
            val body = "${discountRate.toInt()}% 할인 쿠폰 ${couponCount.toInt()}개 선착순!"
            sendNotification(title, body)

            saveNotificationToRoom(title, body, System.currentTimeMillis())

        } catch (e: Exception) {
            Log.e("FCM", "이벤트 실행 중 오류", e)
            sendNotification("🔥 선착순 이벤트 시작!", "지금 바로 참여하세요!")
        }
    }

    private fun sendNotification(title: String, body: String) {
        Log.d("FCM", "알림 전송: $title - $body")

        val channelId = "ticket_channel" // 🔄 알림 유형별로 채널 나눌 수도 있음
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 🔔 채널 생성 (최초 1회)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "이벤트 티켓 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.ssafy.facemeet.client.R.drawable.logo_48) // 알림 아이콘
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt() // ✅ 고유 ID

        notificationManager.notify(notificationId, builder.build())
    }


    private fun parseDateTime(dateTimeStr: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )
        for (formatStr in formats) {
            try {
                return SimpleDateFormat(formatStr, Locale.getDefault()).parse(dateTimeStr)
            } catch (_: Exception) {
            }
        }
        Log.e("FCM", "시간 파싱 실패: $dateTimeStr")
        return null
    }

    private fun parseEventData(eventDataStr: String): Map<String, Any> {
        return try {
            Gson().fromJson(eventDataStr, object : TypeToken<Map<String, Any>>() {}.type)
        } catch (e: Exception) {
            Log.e("FCM", "이벤트 데이터 파싱 실패", e)
            emptyMap()
        }
    }

    private fun storeEvent(settingId: String, triggerTime: Date, eventDataStr: String) {
        val eventInfo = mapOf(
            "settingId" to settingId,
            "triggerTime" to triggerTime.time.toString(),
            "eventData" to eventDataStr
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

                if (triggerTime.time > System.currentTimeMillis()) {
                    scheduleLocalEvent(settingId, triggerTime, eventDataStr)
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


}
