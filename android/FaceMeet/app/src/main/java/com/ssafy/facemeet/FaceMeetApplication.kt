package com.ssafy.facemeet

import android.app.Application
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FaceMeetApplication"
@HiltAndroidApp
class FaceMeetApplication : Application() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var chatWebSocketManager: ChatWebSocketManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            tokenManager.initializeCache()

            val userId = tokenManager.getUserPK()?.toLongOrNull()
            val accessToken = tokenManager.getAccessToken()

            if (userId != null && !accessToken.isNullOrEmpty()) {
                chatWebSocketManager.connect(userId, accessToken)
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onStart(owner: LifecycleOwner) {
                Log.d("App", "앱 포그라운드 복귀")

                // WebSocket 매니저에 포그라운드 상태 알림
                chatWebSocketManager.setAppForegroundState(true)

                // 약간의 지연 후 연결 상태 확인 (화면 전환 완료 대기)
                Handler(Looper.getMainLooper()).postDelayed({
                    applicationScope.launch {
                        val userId = tokenManager.getUserPK()?.toLongOrNull()
                        val accessToken = tokenManager.getAccessToken()

                        if (userId != null && !accessToken.isNullOrEmpty()) {
                            val currentState = chatWebSocketManager.connectionState.value
                            Log.d("App", "현재 연결 상태: $currentState")

                            if (currentState == ConnectionState.DISCONNECTED ||
                                currentState == ConnectionState.ERROR) {
                                Log.d("App", "연결 끊어져 있음 - 재연결 시도")
                                chatWebSocketManager.connect(userId, accessToken)
                            }
                        }
                    }
                }, 500)
            }

            override fun onStop(owner: LifecycleOwner) {
                Log.d("App", "앱 백그라운드 진입")
                chatWebSocketManager.setAppForegroundState(false)

            }
        })
    }
}