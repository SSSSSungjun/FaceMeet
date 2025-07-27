package com.ssafy.facemeet

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.ssafy.facemeet.core.data.datastore.TokenManager
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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch  {
            tokenManager.initializeCache()
        }

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        var keyHash = Utility.getKeyHash(this)
        Log.d(TAG, "onCreate: $keyHash")


    }
}