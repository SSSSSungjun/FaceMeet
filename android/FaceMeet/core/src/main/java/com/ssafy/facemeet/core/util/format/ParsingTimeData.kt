package com.ssafy.facemeet.core.util.format

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
object ParsingTimeData {

    fun String.formatSmartDate(): String {
        val utcZonedDateTime = ZonedDateTime.parse(this) // "2025-07-30T13:42:41.694Z"
        val now = ZonedDateTime.now(ZoneId.systemDefault())

        val localDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
        val inputDate = localDateTime.toLocalDate()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)

        return when (inputDate) {
            today -> localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            yesterday -> "어제"
            else -> {
                if (inputDate.year == today.year) {
                    localDateTime.format(DateTimeFormatter.ofPattern("MM월 dd일"))
                } else {
                    localDateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                }
            }
        }
    }

    fun String.toHourMinuteString(): String =
        ZonedDateTime.parse(this).withZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))


    fun String.toFullDateString(): String =
        ZonedDateTime.parse(this).withZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))


}