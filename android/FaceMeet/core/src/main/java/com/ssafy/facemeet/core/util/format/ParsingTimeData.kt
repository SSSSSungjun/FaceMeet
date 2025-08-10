package com.ssafy.facemeet.core.util.format

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
object ParsingTimeData {

    private const val TAG = "ParsingTimeData"

    fun String.formatSmartDate(): String {
        Log.d(TAG, "formatSmartDate() 시작 - 입력값: '$this'")
        return try {
            val localDateTime = LocalDateTime.parse(this.replace("Z", "").replace("+09:00", ""))
            val now = LocalDateTime.now()

            val inputDate = localDateTime.toLocalDate()
            val today = now.toLocalDate()
            val yesterday = today.minusDays(1)

            val result = when (inputDate) {
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
            Log.d(TAG, "formatSmartDate() 성공 - 결과: '$result'")
            result
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "formatSmartDate() LocalDateTime.parse() 실패", e)
            try {
                // ISO 8601 형식이라면 시간대 변환
                val instant = Instant.parse(this)
                val localDateTime = instant.atOffset(ZoneOffset.ofHours(9)).toLocalDateTime() // UTC+9 (한국)
                val now = LocalDateTime.now()

                // 위와 동일한 로직
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
            } catch (e2: Exception) {
                Log.e(TAG, "formatSmartDate() 모든 시도 실패", e2)
                this
            }
        }
    }

    fun String.toHourMinuteString(): String {
        val TAG = "ParsingTime" // 태그 정의
        Log.d(TAG, "toHourMinuteString() 시작 - 입력값: '$this'")

        // 한국 시간대 ZoneId 정의
        val koreaZoneId = ZoneId.of("Asia/Seoul")

        return try {
            // Instant.parse() 우선 시도
            val result = Instant.parse(this)
                .atZone(koreaZoneId) // <-- 여기를 수정
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            Log.d(TAG, "toHourMinuteString() Instant.parse() 성공 - 결과: '$result'")
            result
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "toHourMinuteString() Instant.parse() 실패 - ZonedDateTime 시도", e)
            try {
                // ZonedDateTime.parse() 재시도
                val result = ZonedDateTime.parse(this)
                    .withZoneSameInstant(koreaZoneId) // <-- 여기를 수정
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                Log.d(TAG, "toHourMinuteString() ZonedDateTime.parse() 성공 - 결과: '$result'")
                result
            } catch (e2: DateTimeParseException) {
                Log.w(TAG, "toHourMinuteString() ZonedDateTime.parse() 실패 - parseFlexibleDateTime 시도", e2)
                // 파싱 실패 시 parseFlexibleDateTime으로 재시도
                parseFlexibleDateTime()
                    ?.withZoneSameInstant(koreaZoneId) // <-- 여기를 수정
                    ?.format(DateTimeFormatter.ofPattern("HH:mm"))
                    ?.also { result ->
                        Log.d(TAG, "toHourMinuteString() parseFlexibleDateTime 성공 - 결과: '$result'")
                    }
                    ?: run {
                        Log.e(TAG, "toHourMinuteString() 모든 시도 실패 - 기본값 반환")
                        "--:--"
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "toHourMinuteString() 예상치 못한 오류", e)
            "--:--"
        }
    }


    fun String.toFullDateString(): String {
        Log.d(TAG, "toFullDateString() 시작 - 입력값: '$this'")
        return try {
            // Instant.parse() 우선 시도
            val result = Instant.parse(this)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
            Log.d(TAG, "toFullDateString() Instant.parse() 성공 - 결과: '$result'")
            result
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "toFullDateString() Instant.parse() 실패 - ZonedDateTime 시도", e)
            try {
                // ZonedDateTime.parse() 재시도
                val result = ZonedDateTime.parse(this)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                Log.d(TAG, "toFullDateString() ZonedDateTime.parse() 성공 - 결과: '$result'")
                result
            } catch (e2: DateTimeParseException) {
                Log.w(TAG, "toFullDateString() ZonedDateTime.parse() 실패 - parseFlexibleDateTime 시도", e2)
                // 파싱 실패 시 parseFlexibleDateTime으로 재시도
                parseFlexibleDateTime()
                    ?.withZoneSameInstant(ZoneId.systemDefault())
                    ?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                    ?.also { result ->
                        Log.d(TAG, "toFullDateString() parseFlexibleDateTime 성공 - 결과: '$result'")
                    }
                    ?: run {
                        Log.e(TAG, "toFullDateString() 모든 시도 실패 - 기본값 반환")
                        "날짜 오류"
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "toFullDateString() 예상치 못한 오류", e)
            "날짜 오류"
        }
    }

    fun String.parseFlexibleDateTime(): ZonedDateTime? {
        Log.d(TAG, "parseFlexibleDateTime() 시작 - 입력값: '$this'")

        // 한국 시간대 ZoneId를 명시적으로 정의
        val koreaZoneId = ZoneId.of("Asia/Seoul")

        // 1. Instant.parse() - 가장 표준적이고 확실한 방법
        try {
            // atZone() 메서드에 한국 시간대 적용
            val result = Instant.parse(this).atZone(koreaZoneId)
            Log.d(TAG, "parseFlexibleDateTime() Instant.parse() 성공")
            return result
        } catch (e: DateTimeParseException) {
            Log.d(TAG, "parseFlexibleDateTime() Instant.parse() 실패 - 다음 방법 시도")
        }

        // 2. 기본 ZonedDateTime.parse() 시도
        try {
            // 파싱된 ZonedDateTime의 시간대도 한국 시간대로 통일
            val result = ZonedDateTime.parse(this).withZoneSameInstant(koreaZoneId)
            Log.d(TAG, "parseFlexibleDateTime() ZonedDateTime.parse() 성공")
            return result
        } catch (e: DateTimeParseException) {
            Log.d(TAG, "parseFlexibleDateTime() ZonedDateTime.parse() 실패 - 커스텀 패턴 시도")
        }

        // 3. 커스텀 패턴들로 시도
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
        )

        for ((index, pattern) in patterns.withIndex()) {
            try {
                Log.d(TAG, "parseFlexibleDateTime() 패턴 시도 #${index + 1}: '$pattern'")
                val formatter = DateTimeFormatter.ofPattern(pattern).withZone(koreaZoneId) // <-- 여기를 수정

                val result = if (pattern.endsWith("'Z'")) {
                    // 'Z'가 포함된 패턴은 UTC로 파싱 후 한국 시간대로 변환
                    LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern)).atZone(koreaZoneId)
                } else {
                    // 다른 형식들은 한국 시간대 포맷터로 바로 파싱
                    ZonedDateTime.parse(this, formatter)
                }
                Log.d(TAG, "parseFlexibleDateTime() 패턴 #${index + 1} 성공")
                return result
            } catch (e: DateTimeParseException) {
                Log.d(TAG, "parseFlexibleDateTime() 패턴 #${index + 1} 실패: ${e.message}")
                continue
            }
        }

        Log.e(TAG, "parseFlexibleDateTime() 모든 패턴 실패 - null 반환")
        return null
    }

    // 안전한 확장 함수들
    fun String.toHourMinuteStringSafe(): String? {
        Log.d(TAG, "toHourMinuteStringSafe() 호출 - 입력값: '$this'")
        return parseFlexibleDateTime()
            ?.withZoneSameInstant(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern("HH:mm"))
            ?.also { result ->
                Log.d(TAG, "toHourMinuteStringSafe() 성공 - 결과: '$result'")
            }
    }

    fun String.toFullDateStringSafe(): String? {
        Log.d(TAG, "toFullDateStringSafe() 호출 - 입력값: '$this'")
        return parseFlexibleDateTime()
            ?.withZoneSameInstant(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
            ?.also { result ->
                Log.d(TAG, "toFullDateStringSafe() 성공 - 결과: '$result'")
            }
    }

    fun String.formatSmartDateSafe(): String? {
        Log.d(TAG, "formatSmartDateSafe() 호출 - 입력값: '$this'")
        val zonedDateTime = parseFlexibleDateTime() ?: return null
        val result = formatSmartDateSafe(zonedDateTime)
        Log.d(TAG, "formatSmartDateSafe() 성공 - 결과: '$result'")
        return result
    }

    private fun formatSmartDateSafe(zonedDateTime: ZonedDateTime): String {
        Log.d(TAG, "formatSmartDateSafe() 내부 함수 호출")
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
        val inputDate = localDateTime.toLocalDate()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)

        val result = when (inputDate) {
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
        Log.d(TAG, "formatSmartDateSafe() 내부 함수 완료 - 결과: '$result'")
        return result
    }

    // 테스트용 함수 - 2025-08-06T06:08:03.375Z 형식으로 업데이트
    fun testParsing() {
        val testDate = "2025-08-06T06:08:03.375Z"
        Log.d(TAG, "=== testParsing() 시작 ===")
        Log.d(TAG, "Original: $testDate")

        Log.d(TAG, "Smart Date 테스트 시작")
        val smartDate = testDate.formatSmartDate()
        Log.d(TAG, "Smart Date 완료: $smartDate")

        Log.d(TAG, "Hour Minute 테스트 시작")
        val hourMinute = testDate.toHourMinuteString()
        Log.d(TAG, "Hour Minute 완료: $hourMinute")

        Log.d(TAG, "Full Date 테스트 시작")
        val fullDate = testDate.toFullDateString()
        Log.d(TAG, "Full Date 완료: $fullDate")

        // 추가 테스트
        Log.d(TAG, "=== Additional Tests ===")
        Log.d(TAG, "Safe Smart Date: ${testDate.formatSmartDateSafe()}")
        Log.d(TAG, "Safe Hour Minute: ${testDate.toHourMinuteStringSafe()}")
        Log.d(TAG, "Safe Full Date: ${testDate.toFullDateStringSafe()}")

        Log.d(TAG, "=== testParsing() 완료 ===")
    }
}