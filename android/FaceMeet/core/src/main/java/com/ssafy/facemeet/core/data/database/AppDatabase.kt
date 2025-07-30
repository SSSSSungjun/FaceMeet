package com.ssafy.facemeet.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssafy.facemeet.core.data.database.entity.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}