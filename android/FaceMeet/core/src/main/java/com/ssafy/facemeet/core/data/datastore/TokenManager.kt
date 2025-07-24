package com.ssafy.facemeet.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
       // private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String? = null,
        userId: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            //userId?.let { preferences[USER_ID_KEY] = it }
        }
        cachedAccessToken = accessToken
        refreshToken?.let { cachedRefreshToken = it }
    }

    suspend fun getAccessToken(): String? {
        return dataStore.data.first()[ACCESS_TOKEN_KEY]
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.first()[REFRESH_TOKEN_KEY]
    }

//    suspend fun getUserId(): String? {
//        return dataStore.data.first()[USER_ID_KEY]
//    }

    suspend fun hasToken(): Boolean {
        val accessToken = getAccessToken()
        return !accessToken.isNullOrEmpty()
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
            !accessToken.isNullOrEmpty()
        }
    }

    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    fun getAccessTokenSync(): String? = cachedAccessToken
    fun getRefreshTokenSync(): String? = cachedRefreshToken

    fun saveTokensSync(accessToken: String, refreshToken: String?) {
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
        CoroutineScope(Dispatchers.IO).launch {
            saveTokens(accessToken, refreshToken)
        }
    }

    fun clearTokensSync() {
        cachedAccessToken = null
        cachedRefreshToken = null
        CoroutineScope(Dispatchers.IO).launch {
            clearTokens()
        }
    }
}