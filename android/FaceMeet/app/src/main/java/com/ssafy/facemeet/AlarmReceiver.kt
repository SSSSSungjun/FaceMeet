package com.ssafy.facemeet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ssafy.facemeet.fcm.FcmAlarmHandler

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val settingId = intent.getStringExtra("settingId") ?: return
        val title = intent.getStringExtra("title")
        val body = intent.getStringExtra("body")

        FcmAlarmHandler.triggerEvent(context, settingId.toLong(), title, body)
    }

}
