package com.ssafy.facemeet.core.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val ONBOARDING_STATE_KEY = stringPreferencesKey("onboarding_state")

    suspend fun getCurrentState(): OnBoardingState {
        val state = dataStore.data.first()[ONBOARDING_STATE_KEY]
            ?: OnBoardingState.NOT_STARTED.name
        return OnBoardingState.valueOf(state)
    }


    suspend fun updateState(state: OnBoardingState) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_STATE_KEY] = state.name
        }
        Log.d("OnboardingManager", "🎯 온보딩 상태 업데이트: $state")
    }


    suspend fun resetState() {
        dataStore.edit { preferences ->
            preferences.remove(ONBOARDING_STATE_KEY)
        }
        Log.d("OnboardingManager", "🎯 온보딩 상태 초기화")
    }

    fun getNextDestination(currentState: OnBoardingState): String {
        return when (currentState) {
            OnBoardingState.NOT_STARTED -> "web_login"
            OnBoardingState.WEB_LOGIN_COMPLETED -> "register"
            OnBoardingState.PROFILE_COMPLETED -> "camera"
            OnBoardingState.FACE_PHOTO_COMPLETED -> "main"
            OnBoardingState.ALL_COMPLETED -> "main"
        }
    }
}

