package com.ssafy.facemeet.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ssafy.facemeet.core.data.database.entity.NotificationEntity
import com.ssafy.facemeet.core.data.database.entity.NotificationTypeConverters

@Database(
    entities = [NotificationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(NotificationTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}