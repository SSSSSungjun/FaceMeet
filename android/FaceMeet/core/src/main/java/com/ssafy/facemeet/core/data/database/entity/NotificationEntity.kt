package com.ssafy.facemeet.core.data.database.entity


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index("settingId")]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val settingId: Long? = null,
    val body: String,
    val triggerTime: Long,
    val receivedTime: Long = System.currentTimeMillis(),
    val type: NotificationType,       // String -> enum
)