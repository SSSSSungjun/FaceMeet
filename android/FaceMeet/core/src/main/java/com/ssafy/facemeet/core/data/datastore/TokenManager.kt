package com.ssafy.facemeet.core.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TokenManager"

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null
    private var cachedUserPk: String? = null

    private val _tokenRefreshResult = MutableSharedFlow<TokenRefreshResult>()
    val tokenRefreshResult: SharedFlow<TokenRefreshResult> = _tokenRefreshResult

    suspend fun initializeCache() {
        val preferences = dataStore.data.first()
        cachedAccessToken = preferences[ACCESS_TOKEN_KEY]
        cachedRefreshToken = preferences[REFRESH_TOKEN_KEY]
        cachedUserPk = preferences[USER_PK_KEY]
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        val userPK = extractUserIdFromToken(accessToken)
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            userPK?.let { preferences[USER_PK_KEY] = it }
        }
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
        cachedUserPk = userPK
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
        return cachedUserPk ?: dataStore.data.first()[USER_PK_KEY]?.also {
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

    suspend fun handleTokenRefreshResult(isSuccess: Boolean, newAccessToken: String? = null, newRefreshToken: String? = null) {
        if (isSuccess && newAccessToken != null && newRefreshToken != null) {
            saveTokens(newAccessToken, newRefreshToken)
            _tokenRefreshResult.emit(TokenRefreshResult.Success)
            Log.d(TAG, "토큰 갱신 성공 이벤트 발행")
        } else {
            clearTokens()
            _tokenRefreshResult.emit(TokenRefreshResult.Failed)
            Log.d(TAG, "토큰 갱신 실패 이벤트 발행")
        }
    }

    private fun extractUserIdFromToken(accessToken: String): String? {
        return try {
            val parts = accessToken.split(".")
            if (parts.size != 3) {
                return null
            }
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
            val userIdPatterns = listOf(
                Regex("\"currentUser\":\\s*\"?(\\d+)\"?"),
                Regex("\"sub\":\\s*\"?(\\d+)\"?"),
                Regex("\"userId\":\\s*\"?(\\d+)\"?"),
                Regex("\"id\":\\s*\"?(\\d+)\"?"),
                Regex("\"user_id\":\\s*\"?(\\d+)\"?"),
                Regex("\"userPK\":\\s*\"?(\\d+)\"?")
            )
            for (pattern in userIdPatterns) {
                val match = pattern.find(payload)
                if (match != null) {
                    return match.groupValues[1]
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_PK_KEY = stringPreferencesKey("user_pk")
    }
}
