package com.ssafy.facemeet.client.ui.matching

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun MatchingLoadingScreen(
    onMatchFound: (String) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    // 배경 그라데이션
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF6C0CA), Color(0xFFACD9F9)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        // 뒤로가기 버튼
        IconButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                tint = CommonColor.Brown,
                contentDescription = "뒤로가기"
            )
        }

        // 중앙 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "당신과 궁합이\n가장 잘 맞는 분을 찾고 있어요",
                fontSize = 16.sp,
                color = CommonColor.Brown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            RotatingStarWithBouncingHeart()

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "잠시만 기다려주세요",
                fontSize = 16.sp,
                color = CommonColor.Brown
            )
        }
    }
}

@Composable
fun RotatingStarWithBouncingHeart() {
    val infiniteTransition = rememberInfiniteTransition()

    // 별 회전
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 하트 크기 변화
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // 회전하는 별
        Image(
            painter = painterResource(id = R.drawable.ic_loading_star),
            contentDescription = "로딩 별",
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )

        // 커졌다 작아졌다 하는 기울어진 하트
        Image(
            painter = painterResource(id = R.drawable.ic_loading_heart),
            contentDescription = "하트",
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = -25f
                }
                .align(Alignment.TopStart)
                .offset(x = (-6).dp, y = (-6).dp)
        )
    }
}