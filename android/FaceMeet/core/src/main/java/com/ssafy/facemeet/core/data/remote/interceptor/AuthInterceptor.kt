package com.ssafy.facemeet.core.data.remote.interceptor

import com.ssafy.facemeet.core.data.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            val token = runBlocking { tokenManager.getAccessToken() }
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(request)


    }
}