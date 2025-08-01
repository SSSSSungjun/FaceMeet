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
            // 파싱 실패 시 parseFlexibleDateTime으로 재시도
            parseFlexibleDateTime()?.let { zonedDateTime ->
                formatSmartDateSafe(zonedDateTime)
            } ?: this
        } catch (e: Exception) {
            this
        }
    }

    fun String.toHourMinuteString(): String {
        return try {
            ZonedDateTime.parse(this)
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: DateTimeParseException) {
            // 파싱 실패 시 parseFlexibleDateTime으로 재시도
            parseFlexibleDateTime()
                ?.withZoneSameInstant(ZoneId.systemDefault())
                ?.format(DateTimeFormatter.ofPattern("HH:mm"))
                ?: "--:--"
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
            // 파싱 실패 시 parseFlexibleDateTime으로 재시도
            parseFlexibleDateTime()
                ?.withZoneSameInstant(ZoneId.systemDefault())
                ?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                ?: "날짜 오류"
        } catch (e: Exception) {
            "날짜 오류"
        }
    }

    // 수정된 parseFlexibleDateTime - 더 정확한 패턴 순서
    fun String.parseFlexibleDateTime(): ZonedDateTime? {
        // 먼저 기본 ISO 파서로 시도 (가장 표준적인 방법)
        try {
            return ZonedDateTime.parse(this)
        } catch (e: DateTimeParseException) {
            // 기본 파서 실패 시 커스텀 패턴들 시도
        }

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",     // 밀리초 3자리 (일반적)
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",  // 마이크로초 6자리
            "yyyy-MM-dd'T'HH:mm:ss'Z'",         // 초 단위
            "yyyy-MM-dd'T'HH:mm:ssXXX",         // 타임존 포함
            "yyyy-MM-dd'T'HH:mm:ss",            // 로컬 시간
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'",       // 밀리초 1자리
            "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",      // 밀리초 2자리
        )

        for (pattern in patterns) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)
                return ZonedDateTime.parse(this, formatter.withZone(ZoneId.of("UTC")))
            } catch (e: DateTimeParseException) {
                continue
            }
        }

        return null
    }

    // 안전한 확장 함수들
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
        return formatSmartDateSafe(zonedDateTime)
    }

    private fun formatSmartDateSafe(zonedDateTime: ZonedDateTime): String {
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

    // 테스트용 함수 (개발 중에만 사용)
    fun testParsing() {
        val testDate = "2025-07-31T11:59:49.654Z"
        println("Original: $testDate")
        println("Smart Date: ${testDate.formatSmartDate()}")
        println("Hour Minute: ${testDate.toHourMinuteString()}")
        println("Full Date: ${testDate.toFullDateString()}")
    }
}