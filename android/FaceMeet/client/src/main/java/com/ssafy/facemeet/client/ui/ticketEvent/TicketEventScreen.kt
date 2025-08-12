@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.facemeet.client.ui.ticketEvent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun TicketEventScreen(
    onAcquire: () -> Unit,
    settingId: Long,
    modifier: Modifier = Modifier
) {
    val bg = CommonColor.Beige100

    Scaffold(
        containerColor = bg
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f)) // 상단 여백 자동 비율

            // 중앙 티켓 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_ticket_translate),
                contentDescription = "Event Ticket",
                modifier = Modifier.size(260.dp)
            )

            Spacer(Modifier.weight(1f)) // 버튼 전까지 하단 여백 자동 비율

            // 버튼
            TicketAcquireButton(
                onClick = onAcquire,
                buttonText = "획득하기",
                settingId = settingId
            )
        }
    }
}

@Composable
fun TicketAcquireButton(
    onClick: () -> Unit,
    buttonText: String,
    settingId: Long
) {
    val isDisabled = false

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp) // 그림자 공간 확보
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x66D2691E),
                spotColor = Color(0x66D2691E)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDisabled) {
                        listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E)) // 비활성 회색
                    } else {
                        listOf(Color(0xFFD2691E), Color(0xFFE88D4C)) // 오렌지 그라디언트
                    }
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .height(64.dp)
            .clickable(enabled = !isDisabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDisabled) Color(0xFFEEEEEE) else Color.White
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEF5)
@Composable
private fun TicketEventScreenPreview() {
    TicketEventScreen(
        onAcquire = {},settingId=1
    )
}