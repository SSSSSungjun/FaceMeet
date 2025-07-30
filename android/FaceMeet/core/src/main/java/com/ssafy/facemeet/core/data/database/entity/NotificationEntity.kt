package com.ssafy.facemeet.core.data.database.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val triggerTime: Long,
    val receivedTime: Long = System.currentTimeMillis(),
    val type: String = "default" // 새 필드 추가
)
