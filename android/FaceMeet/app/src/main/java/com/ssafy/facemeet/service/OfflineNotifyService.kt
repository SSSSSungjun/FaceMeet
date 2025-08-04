package com.ssafy.facemeet.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ssafy.facemeet.core.domain.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OfflineNotifyService : Service() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("OfflineNotifyService", "✅ 앱 스와이프 종료 감지됨")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                userRepository.postOffline()
                Log.d("OfflineNotifyService", "오프라인 전송 완료")
            } catch (e: Exception) {
                Log.e("OfflineNotifyService", "오프라인 전송 실패", e)
            }
        }

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
