package com.ssafy.facemeet.core.network.interceptor
import com.ssafy.facemeet.core.network.PersistentCookieJar
import com.ssafy.facemeet.core.repository.auth.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthErrorInterceptor @Inject constructor(
    private val authRepository: AuthRepository,  // Repository 사용
    private val cookieJar: PersistentCookieJar
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val noAuthPaths = listOf("/api/v1/auth/signup", "/api/v1/auth/refresh", "/api/v1/auth/logout") //추후 건드려야할듯
        val shouldSkipAuth = noAuthPaths.any { path ->
            request.url.encodedPath.contains(path)
        }

        if (response.code == 401 && !shouldSkipAuth) {
            response.close()

            return try {
                val refreshResult = runBlocking {
                    authRepository.refreshToken()  // Repository의 refreshToken 사용
                }

                if (refreshResult.isSuccess) {
                    chain.proceed(request)  // 재시도
                } else {
                    chain.proceed(request)  // 401 그대로 전달
                }
            } catch (e: Exception) {
                chain.proceed(request)
            }
        }

        return response
    }
}