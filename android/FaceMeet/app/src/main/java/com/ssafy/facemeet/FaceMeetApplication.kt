package com.ssafy.facemeet

import android.app.Application
import com.ssafy.facemeet.core.data.datastore.TokenManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FaceMeetApplication"

@HiltAndroidApp
class FaceMeetApplication : Application() {

    @Inject
    lateinit var tokenManager: TokenManager
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch  {
            tokenManager.initializeCache()
        }
    }
}

