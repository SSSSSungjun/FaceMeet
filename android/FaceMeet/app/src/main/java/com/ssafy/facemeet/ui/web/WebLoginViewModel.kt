package com.ssafy.facemeet.ui.web

// 아래는 요청/응답 DTO에 따라 경로를 조정하세요
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.remote.api.FcmService
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.data.remote.dto.response.fcm.FcmTokenResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class WebLoginViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val fcmService: FcmService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun saveToken(refreshToken: String, accessToken: String) {
        viewModelScope.launch {
            tokenManager.saveTokens(accessToken, refreshToken)

            // 토큰 저장후 (인증 엑세스토큰을 가지고) fcm 토큰 서버에 보냄
            putFcmToken()
        }
    }

    private suspend fun putFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            val request = FcmTokenRequest(
                deviceToken = token,
                deviceType = "android",
                deviceId = deviceId
            )

            fcmService.registerDevice(request).enqueue(object : Callback<FcmTokenResponse> {
                override fun onResponse(
                    call: Call<FcmTokenResponse>,
                    response: Response<FcmTokenResponse>
                ) {
                    Log.d("FCM", "FCM 토큰 등록 성공: ${response.body()}")
                }

                override fun onFailure(call: Call<FcmTokenResponse>, t: Throwable) {
                    Log.e("FCM", "FCM 토큰 등록 실패", t)
                }
            })

        } catch (e: Exception) {
            Log.e("FCM", "FCM 토큰 가져오기 실패", e)
        }
    }

}