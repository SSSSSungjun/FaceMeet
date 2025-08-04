package com.ssafy.facemeet.core.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TokenManager"

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_PK = stringPreferencesKey("user_pk")
    }

    // 메모리 캐시
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null
    private var cachedUserPk: String? = null

    // 토큰 갱신 결과를 알리는 Flow
    private val _tokenRefreshResult = MutableSharedFlow<TokenRefreshResult>()
    val tokenRefreshResult: SharedFlow<TokenRefreshResult> = _tokenRefreshResult

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String? = null,
    ) {
        // JWT 토큰에서 사용자 ID 추출
        val userPK = extractUserIdFromToken(accessToken)

        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }

            // 추출한 사용자 ID 저장
            userPK?.let { preferences[USER_PK] = it }
        }

        cachedAccessToken = accessToken
        refreshToken?.let { cachedRefreshToken = it }
        userPK?.let { cachedUserPk = it }

        Log.d(TAG, "saveTokens: 추출된 userPK = $userPK")
    }

    private fun extractUserIdFromToken(accessToken: String): String? {
        return try {
            val parts = accessToken.split(".")
            if (parts.size == 3) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                Log.d(TAG, "JWT payload: $payload")

                // currentUser 패턴으로 사용자 ID 추출
                val userIdPatterns = listOf(
                    Regex("\"currentUser\":\\s*(\\d+)"),
                    Regex("\"sub\":\\s*\"?(\\d+)\"?"),
                    Regex("\"userId\":\\s*\"?(\\d+)\"?"),
                    Regex("\"id\":\\s*\"?(\\d+)\"?"),
                    Regex("\"user_id\":\\s*\"?(\\d+)\"?"),
                    Regex("\"userPK\":\\s*\"?(\\d+)\"?")
                )

                for (pattern in userIdPatterns) {
                    val match = pattern.find(payload)
                    if (match != null) {
                        val userId = match.groupValues[1]
                        Log.d(TAG, "토큰에서 추출한 사용자 ID: $userId")
                        return userId
                    }
                }

                Log.w(TAG, "토큰에서 사용자 ID를 찾을 수 없음")
                return null
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "토큰에서 사용자 ID 추출 오류: ${e.message}")
            null
        }
    }

    suspend fun getAccessToken(): String? {
        return cachedAccessToken ?: dataStore.data.first()[ACCESS_TOKEN_KEY]?.also {
            cachedAccessToken = it
        }
    }

    suspend fun getRefreshToken(): String? {
        return cachedRefreshToken ?: dataStore.data.first()[REFRESH_TOKEN_KEY]?.also {
            cachedRefreshToken = it
        }
    }

    suspend fun getUserPK(): String? {
        return cachedUserPk ?: dataStore.data.first()[USER_PK]?.also {
            cachedUserPk = it
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedUserPk = null
    }

    fun isLoggedInFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN_KEY]
            val refreshToken = preferences[REFRESH_TOKEN_KEY]
            !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()
        }
    }

    // ===== Synchronous methods for Authenticator =====

    fun getAccessTokenSync(): String? = cachedAccessToken
    fun getRefreshTokenSync(): String? = cachedRefreshToken

    //TokenAuthenticator에서 호출 - 토큰 갱신 성공 시
    fun saveTokensSync(accessToken: String, refreshToken: String?) {
        val previousAccessToken = cachedAccessToken

        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken

        // 새 토큰에서 사용자 ID 추출하여 캐시 업데이트
        val userPK = extractUserIdFromToken(accessToken)
        userPK?.let { cachedUserPk = it }

        CoroutineScope(Dispatchers.IO).launch {
            saveTokens(accessToken, refreshToken)

            // 토큰이 실제로 변경되었는지 확인 후 성공 알림
            if (previousAccessToken != accessToken) {
                _tokenRefreshResult.emit(TokenRefreshResult.Success)
            }
        }
    }

    //TokenAuthenticator에서 호출 - 토큰 갱신 실패 시
    fun clearTokensSync() {
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedUserPk = null

        CoroutineScope(Dispatchers.IO).launch {
            clearTokens()
            // 토큰 갱신 실패 알림
            _tokenRefreshResult.emit(TokenRefreshResult.Failed)
        }
    }

    //앱 시작 시 캐시 초기화
    suspend fun initializeCache() {
        val preferences = dataStore.data.first()
        cachedAccessToken = preferences[ACCESS_TOKEN_KEY]
        cachedRefreshToken = preferences[REFRESH_TOKEN_KEY]
        cachedUserPk = preferences[USER_PK]

        Log.d(TAG, "캐시 초기화 - userPK: $cachedUserPk")
    }
}