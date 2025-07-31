package com.ssafy.facemeet.core.util.format

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
object ParsingTimeData {

    fun String.formatSmartDate(): String {
        return try {
            val utcZonedDateTime = ZonedDateTime.parse(this)
            val now = ZonedDateTime.now(ZoneId.systemDefault())

            val localDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
            val inputDate = localDateTime.toLocalDate()
            val today = now.toLocalDate()
            val yesterday = today.minusDays(1)

            when (inputDate) {
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
        } catch (e: DateTimeParseException) {
            // 파싱 실패 시 원본 문자열 반환 또는 기본값
            this
        } catch (e: Exception) {
            // 기타 예외 처리
            this
        }
    }

    fun String.toHourMinuteString(): String {
        return try {
            ZonedDateTime.parse(this)
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: DateTimeParseException) {
            // 파싱 실패 시 기본값 반환
            "--:--"
        } catch (e: Exception) {
            "--:--"
        }
    }

    fun String.toFullDateString(): String {
        return try {
            ZonedDateTime.parse(this)
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        } catch (e: DateTimeParseException) {
            // 파싱 실패 시 기본값 반환
            "날짜 오류"
        } catch (e: Exception) {
            "날짜 오류"
        }
    }

    // 추가: 다양한 날짜 형식 지원
    fun String.parseFlexibleDateTime(): ZonedDateTime? {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",  // 마이크로초 포함
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",     // 밀리초 포함
            "yyyy-MM-dd'T'HH:mm:ss'Z'",         // 초 단위
            "yyyy-MM-dd'T'HH:mm:ssXXX",         // 타임존 포함
            "yyyy-MM-dd'T'HH:mm:ss",            // 로컬 시간
        )

        for (pattern in patterns) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)
                return ZonedDateTime.parse(this, formatter.withZone(ZoneId.of("UTC")))
            } catch (e: DateTimeParseException) {
                continue
            }
        }

        // ISO 기본 파서로 한 번 더 시도
        return try {
            ZonedDateTime.parse(this)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    // 안전한 확장 함수들 (파싱 실패 시 null 반환)
    fun String.toHourMinuteStringSafe(): String? {
        return parseFlexibleDateTime()
            ?.withZoneSameInstant(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun String.toFullDateStringSafe(): String? {
        return parseFlexibleDateTime()
            ?.withZoneSameInstant(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
    }

    fun String.formatSmartDateSafe(): String? {
        val zonedDateTime = parseFlexibleDateTime() ?: return null
        val now = ZonedDateTime.now(ZoneId.systemDefault())

        val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
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
}