package com.ssafy.facemeet.client.ui.notification

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.core.data.remote.dto.response.NotificationResponse
import com.ssafy.facemeet.core.util.constant.CommonColor
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onNavigateToTicketEvent: (Long) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "알림목록",
                        fontSize = 16.sp,
                        color = CommonColor.Brown500,
                        fontFamily = ChosunCentennial
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = CommonColor.Brown500
                        )
                    }
                }
                // actions 파라미터 없음 (제거)
            )
        }

    ) { padding ->
        when {
            state.loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.error ?: "",
                    color = CommonColor.Gray400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("다시 시도") }
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(state.items) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            // type 판별
                            notification.settingId?.let(onNavigateToTicketEvent)

                            // 읽음 처리
                            if (!notification.isRead) viewModel.markAsRead(notification.notificationId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationResponse,
    onClick: () -> Unit
) {
    // settingId 있으면 이벤트(티켓 아이콘), 없으면 기본 아이콘
    val iconRes = if (notification.settingId != null) {
        R.drawable.ic_notification_ticket
    } else {
        R.drawable.ic_notification_default
    }

    val timestamp = extractTimestamp(notification) // Long? (null이면 시간 미표시)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (notification.isRead) CommonColor.Gray200 else Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "알림 아이콘",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = CommonColor.Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CommonColor.Gray900
                )
                // ✅ 시간은 timestamp가 있을 때만 노출
                Spacer(modifier = Modifier.height(15.dp))
                Log.d(TAG, "NotificationItem: ${timestamp}")
                if (timestamp != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = getRelativeTime(timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CommonColor.Gray400
                    )
                }
            }
        }
//        Divider(
//            color = CommonColor.Gray300,
//            thickness = 1.dp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(start = 16.dp, end = 16.dp) // = 60.dp
//        )
    }
}

private const val TAG = "NotificationScreen"

private fun extractTimestamp(n: NotificationResponse): Long? {
    val md = n.messageData ?: return null

    fun parseCandidate(s: String?): Long? {
        if (s.isNullOrBlank()) return null
        s.toLongOrNull()?.let { return it }           // 이미 epoch millis 문자열인 경우
        return parseIsoToMillis(s)                    // ISO 포맷 파싱
    }

    // ✅ 우선순위: triggerTime → timestamp
    return parseCandidate(md.triggerTime) ?: parseCandidate(md.timestamp)
}

/** 다양한 ISO 문자열을 안전하게 millis로 변환 */
private fun parseIsoToMillis(iso: String): Long? {
    return runCatching {
        OffsetDateTime.parse(iso).toInstant().toEpochMilli()      // ...+09:00 / ...Z
    }.recoverCatching {
        Instant.parse(iso).toEpochMilli()                         // ...Z
    }.recoverCatching {
        ZonedDateTime.parse(iso).toInstant().toEpochMilli()       // ...[Asia/Seoul]
    }.recoverCatching {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() // 타임존 없음
    }.recoverCatching {
        LocalDateTime.parse(iso, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() // 초 미포함
    }.getOrNull()
}

private fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0L)
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)
    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days < 7 -> "${days}일 전"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
    }
}
