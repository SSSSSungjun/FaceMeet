// client/ui/ticketEvent/TicketEventScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.facemeet.client.ui.ticketEvent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun TicketEventScreen(
    settingId: Long,
    modifier: Modifier = Modifier,
    viewModel: MatchTicketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ 화면 들어오면 바로 호출 (settingId 바뀔 때만 재실행)
    LaunchedEffect(settingId) {
        viewModel.take(settingId)
    }

    when (val s = uiState) {
        is MatchTicketViewModel.UiState.Taken -> TicketSuccessScreen()
        is MatchTicketViewModel.UiState.Error -> TicketErrorScreen(message = s.message)
        else -> TicketDefaultScreen( // Idle, Loading
            settingId = settingId,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@Composable
private fun TicketDefaultScreen(
    settingId: Long,
    viewModel: MatchTicketViewModel,
    modifier: Modifier = Modifier
) {
    val bg = CommonColor.Beige100
    val uiState by viewModel.uiState.collectAsState()
    val loading =
        uiState is MatchTicketViewModel.UiState.Loading || uiState is MatchTicketViewModel.UiState.Idle

    Scaffold(containerColor = bg) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ticket_translate),
                contentDescription = "Event Ticket",
                modifier = Modifier.size(260.dp)
            )
            Spacer(Modifier.height(32.dp))
            if (loading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("요청 중…", fontSize = 16.sp, color = CommonColor.Gray500)
            }
        }
    }
}

/* 로딩 오버레이가 필요하면 여기 사용 */
@Composable
private fun LoadingOverlay() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

/* ✅ 성공 풀스크린 (샘플 이미지 스타일) */
@Composable
private fun TicketSuccessScreen() {
    // 부드러운 라디얼(중앙 밝음 → 테두리 아이보리)
    val bg = Brush.radialGradient(
        colors = listOf(Color(0xFFFFF7E8), Color(0xFFFAF4E6), Color(0xFFF6EEDD)),
        center = androidx.compose.ui.geometry.Offset.Unspecified,
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ticket_translate),
                contentDescription = null,
                modifier = Modifier.size(260.dp)
            )
            Spacer(Modifier.height(64.dp))
            Text(
                text = "매칭권을 획득했습니다!",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = CommonColor.Gray900,
                fontFamily = ChosunCentennial
            )
        }
    }
}

/* ✅ 에러 풀스크린 (붉은/살몬톤 배경 + 단색 티켓) */
@Composable
private fun TicketErrorScreen(message: String) {
    val bg = Color(0xFFF5B39B) // 살몬/붉은 톤
    val ticketTint = Color(0xFFECC6B6) // 연한 살몬 (모노크롬)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ticket_translate),
                contentDescription = null,
                modifier = Modifier.size(260.dp),
                colorFilter = ColorFilter.tint(ticketTint, BlendMode.SrcIn)
            )
            Spacer(Modifier.height(64.dp))
            Text(
                text = message,
                fontSize = 18.sp,
                color = CommonColor.Gray900,
                fontFamily = ChosunCentennial
            )
        }
    }
}

/* 버튼 (동일 디자인, enabled만 제어) */
@Composable
fun TicketAcquireButton(
    onClick: () -> Unit,
    buttonText: String,
    enabled: Boolean
) {
    val isDisabled = !enabled
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x66D2691E),
                spotColor = Color(0x66D2691E)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDisabled)
                        listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
                    else
                        listOf(Color(0xFFD2691E), Color(0xFFE88D4C))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .height(64.dp)
            .clickable(enabled = !isDisabled) { onClick() },
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
