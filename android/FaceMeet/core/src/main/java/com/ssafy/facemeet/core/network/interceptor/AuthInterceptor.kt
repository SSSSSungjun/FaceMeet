package com.ssafy.facemeet.core.network.interceptor

import com.ssafy.facemeet.core.local.datastore.TokenManager
import com.ssafy.facemeet.core.network.api.AuthApiService
import com.ssafy.facemeet.core.network.dto.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // refresh API는 토큰 불필요
        val noAuthPaths = listOf("/auth/refresh")
        val needsAuth = noAuthPaths.none { originalRequest.url.encodedPath.contains(it) }

        if (!needsAuth) {
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking { tokenManager.getAccessToken() }

        if (accessToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // 토큰 헤더 추가
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // 401 에러 시 토큰만 클리어 (갱신은 Repository에서 처리)
        if (response.code == 401) {
            runBlocking { tokenManager.clearTokens() }
        }

        return response
    }
}