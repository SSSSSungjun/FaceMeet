package com.ssafy.facemeet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

private const val TAG = "FaceMeetApplication"

@HiltAndroidApp
class FaceMeetApplication : Application(){
    override fun onCreate() {
        super.onCreate()
    }
}