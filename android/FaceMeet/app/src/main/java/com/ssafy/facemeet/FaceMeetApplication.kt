package com.ssafy.facemeet

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.HiltAndroidApp

private const val TAG = "FaceMeetApplication"

@HiltAndroidApp
class FaceMeetApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // 로그 확인용 (디버그 빌드에서만)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "KeyHash: ${Utility.getKeyHash(this)}")
        }
    }
}