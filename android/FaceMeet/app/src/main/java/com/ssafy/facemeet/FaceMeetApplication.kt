package com.ssafy.facemeet

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.domain.repository.UserRepository
import com.ssafy.facemeet.core.domain.usecase.ConnectChatWebSocketUseCase
import com.ssafy.facemeet.core.util.AppStateManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FaceMeetApplication"

@HiltAndroidApp
@RequiresApi(Build.VERSION_CODES.O)
class FaceMeetApplication : Application() {
    @Inject
    lateinit var tokenManager: TokenManager
    @Inject
    lateinit var connectChatWebSocketUseCase: ConnectChatWebSocketUseCase
    @Inject
    lateinit var userRepository: UserRepository

    //방 다시 들어가 떄 갱신되도록해야한다.

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initializeApplication()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    private val appLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Log.d(TAG, "앱이 포그라운드로 전환되었습니다. WebSocket 재연결 시도")


            applicationScope.launch {
                if(tokenManager.getAccessToken()!=null){
                    connectWebSocket()
                    userRepository.postOnline()
                }
            }
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            applicationScope.launch {
                if(tokenManager.getAccessToken()!=null){
                    connectWebSocket()
                    userRepository.postOnline()
                }
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            applicationScope.launch {
                if(tokenManager.getAccessToken()!=null){
                    connectWebSocket()
                    userRepository.postOffline()
                }
            }
        }
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            Log.d("AppState", "onStop: dd")
            AppStateManager.clearCurrentScreen()
            applicationScope.launch {
                if(tokenManager.getAccessToken()!=null){
                    connectWebSocket()
                    userRepository.postOffline()
                }
            }
        }
    }

    private fun initializeApplication() {
        applicationScope.launch {
            tokenManager.initializeCache()
            Log.d(TAG, "토큰 캐시 초기화 완료")
            connectWebSocket()
        }
    }

    private fun connectWebSocket() {
        applicationScope.launch {
            val userId = tokenManager.getUserPK()?.toLongOrNull()
            val accessToken = tokenManager.getAccessToken()

            if (userId != null && !accessToken.isNullOrEmpty()) {
                Log.d(TAG, "userId: $userId, accessToken 유효. WebSocket 연결 시작.")
                connectChatWebSocketUseCase.invoke(userId, accessToken)
            } else {
                Log.w(TAG, "userId 또는 accessToken이 유효하지 않아 WebSocket 연결을 건너뜁니다.")
            }
        }
    }

}