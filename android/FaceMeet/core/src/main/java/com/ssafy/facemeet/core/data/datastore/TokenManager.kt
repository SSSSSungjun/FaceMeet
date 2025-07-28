package com.ssafy.facemeet.core.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val REGISTRATION_COMPLETE = booleanPreferencesKey("registration_complete")
    }

    // 메모리 캐시
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    // 토큰 갱신 결과를 알리는 Flow
    private val _tokenRefreshResult = MutableSharedFlow<TokenRefreshResult>()
    val tokenRefreshResult: SharedFlow<TokenRefreshResult> = _tokenRefreshResult

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String? = null,
        isRegistration : Boolean = false
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            preferences[REGISTRATION_COMPLETE] = isRegistration
        }
        cachedAccessToken = accessToken
        refreshToken?.let { cachedRefreshToken = it }
    }

    suspend fun completeRegistration(isRegistration: Boolean) {
        dataStore.edit { preferences ->
            preferences[REGISTRATION_COMPLETE] = true // 등록 완료 표시
        }
        Log.d("TokenDataStore", "등록 완료 정보 저장")
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

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        cachedAccessToken = null
        cachedRefreshToken = null
    }

    fun isLoggedInFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN_KEY]
            val refreshToken = preferences[REFRESH_TOKEN_KEY]
            val isRegistration =preferences[REGISTRATION_COMPLETE]
            !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty() && isRegistration == true
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
    }
}
