package com.ssafy.facemeet.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssafy.facemeet.core.data.database.entity.NotificationEntity

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE triggerTime <= :now ORDER BY triggerTime DESC")
    suspend fun getPastNotifications(now: Long = System.currentTimeMillis()): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)
}