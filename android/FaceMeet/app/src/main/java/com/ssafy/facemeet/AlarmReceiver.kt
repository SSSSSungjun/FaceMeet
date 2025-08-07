package com.ssafy.facemeet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.ssafy.facemeet.core.data.database.AppDatabase
import com.ssafy.facemeet.fcm.FcmAlarmHandler

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "your_db_name"
        ).build()

        val dao = db.notificationDao()

        val settingId = intent.getStringExtra("settingId") ?: return
        val eventDataStr = intent.getStringExtra("eventDataStr") ?: return
        val title = intent.getStringExtra("title")
        val body = intent.getStringExtra("body")

        FcmAlarmHandler.triggerEvent(context, dao, settingId, eventDataStr, title, body)
    }

}
