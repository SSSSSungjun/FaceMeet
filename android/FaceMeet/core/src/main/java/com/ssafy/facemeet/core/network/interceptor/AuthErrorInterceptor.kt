package com.ssafy.facemeet.core.network.interceptor

import com.ssafy.facemeet.core.network.PersistentCookieJar
import com.ssafy.facemeet.core.repository.auth.AuthRepository
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthErrorInterceptor @Inject constructor(
    private val authRepository: Lazy<AuthRepository>, // 타입 명시
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val noAuthPaths = listOf("/api/v1/auth/signup", "/api/v1/auth/refresh", "/api/v1/auth/logout")
        val shouldSkipAuth = noAuthPaths.any { path ->
            request.url.encodedPath.contains(path)
        }

        if (response.code == 401 && !shouldSkipAuth) {
            response.close()

            return try {
                val refreshResult = runBlocking {
                    authRepository.get().refreshToken()
                }

                if (refreshResult.isSuccess) {
                    chain.proceed(request)
                } else {
                    chain.proceed(request)
                }
            } catch (e: Exception) {
                chain.proceed(request)
            }
        }

        return response
    }
}