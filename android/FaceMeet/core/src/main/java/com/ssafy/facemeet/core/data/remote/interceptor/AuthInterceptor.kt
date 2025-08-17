package com.ssafy.facemeet.core.data.remote.interceptor

import android.util.Log
import com.ssafy.facemeet.core.data.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        
        // ✅ refresh 요청은 Authorization 절대 금지
        if (path.contains("/auth/refresh")) {
            return chain.proceed(
                original.newBuilder()
                    .removeHeader("Authorization")
                    .build()
            )
        }


        val request = chain.request().newBuilder().apply {
            val token = runBlocking { tokenManager.getAccessToken() }
            Log.d("AuthInterceptor", "intercepted with token: $token")
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(request)


    }
}