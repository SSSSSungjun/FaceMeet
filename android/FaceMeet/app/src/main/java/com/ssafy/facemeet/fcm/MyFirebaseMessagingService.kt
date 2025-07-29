package com.ssafy.facemeet.fcm

import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.ssafy.facemeet.core.data.remote.api.FcmService
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.FcmTokenResponse
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmService: FcmService

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "onNewToken: $token")

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val request =
            FcmTokenRequest(deviceToken = token, deviceType = "android", deviceId = deviceId)

        fcmService.registerDevice(request).enqueue(object : Callback<FcmTokenResponse> {
            override fun onResponse(
                call: Call<FcmTokenResponse>,
                response: Response<FcmTokenResponse>
            ) {
                Log.d("FCM", "성공: ${response.body()}")
            }

            override fun onFailure(call: Call<FcmTokenResponse>, t: Throwable) {
                Log.e("FCM", "실패", t)
            }
        })
    }
}
