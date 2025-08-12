package com.ssafy.facemeet.core.data.database.entity

import androidx.room.TypeConverter

enum class NotificationType { TICKET }


class NotificationTypeConverters {
    @TypeConverter
    fun toType(value: String?): NotificationType =
        if (value?.equals("ticket", ignoreCase = true) == true) NotificationType.TICKET
        else NotificationType.TICKET // 현재는 하나뿐이라 기본값 유지

    @TypeConverter
    fun fromType(type: NotificationType?): String = "ticket"
}