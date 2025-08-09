package com.ssafy.facemeet.client.ui.matching

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.core.util.constant.CommonColor
import kotlin.math.sin

@Composable
fun MatchingLoadingScreen(
    onMatchFound: (Long) -> Unit = {},
    onCancel: () -> Unit = {},
    viewModel: MatchingViewModel = hiltViewModel(),
) {
    val matchedChatRoomId by viewModel.matchedChatRoomId.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startMatching()
    }

    LaunchedEffect(matchedChatRoomId) {
        matchedChatRoomId?.let {
            onMatchFound(it)
            viewModel.resetMatchingResult()
        }
    }


    if (error != null) {
        NoMoreMatchesScreen(
            message = error!!,               // ← 파싱된 message 보여줌
        )
        return
    }

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
                tint = CommonColor.Brown500,
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
                color = CommonColor.Brown500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            RotatingStarWithBouncingHeart()

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "잠시만 기다려주세요",
                fontSize = 16.sp,
                color = CommonColor.Brown500
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

@Composable
fun NoMoreMatchesScreen(
    message: String, // ← 에러 메시지 파라미터 추가
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFFDBCFC8),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // 메시지 표시
        Text(
            text = message,
            color = CommonColor.Gray500,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        FallingLeavesDiagonal(
            leafs = listOf(R.drawable.ic_leaf),
            count = 8,
            minSize = 32.dp,
            maxSize = 56.dp,
            durationMsRange = 8000..12000,
            driftDp = 22.dp,
            swayHorizontalDp = 20.dp,
            flutterVerticalDp = 8.dp,
            startTopInsetRatio = 0.12f,
            endBottomInsetRatio = 0.22f
        )
    }
}


@Composable
fun FallingLeavesDiagonal(
    leafs: List<Int>,
    count: Int = 9,
    minSize: Dp = 18.dp,
    maxSize: Dp = 32.dp,
    durationMsRange: IntRange = 8000..12000,
    driftDp: Dp = 22.dp,           // 기본 바람 세기
    swayHorizontalDp: Dp = 20.dp,  // 좌우 스웨이 폭
    flutterVerticalDp: Dp = 8.dp,  // 세로 들썩
    startTopInsetRatio: Float = 0.12f,
    endBottomInsetRatio: Float = 0.22f,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        val drift = with(density) { driftDp.toPx() }
        val swayH = with(density) { swayHorizontalDp.toPx() }
        val flutterV = with(density) { flutterVerticalDp.toPx() }

        val yStart = h * startTopInsetRatio
        val yEnd = h * (1f - endBottomInsetRatio)

        repeat(count) { i ->
            val rnd = remember(i) { kotlin.random.Random(i * 71 + 11) }
            val leafRes = leafs[i % leafs.size]
            val sizeDp = remember { lerp(minSize, maxSize, rnd.nextFloat()) }
            val dur = remember { durationMsRange.random(rnd) }
            val startDelay = remember { rnd.nextInt(0, 20000) }

            // 위상
            val p1 = remember { rnd.nextFloat() }
            val p2 = remember { rnd.nextFloat() }

            val infinite = rememberInfiniteTransition(label = "leaf-$i")
            val t by infinite.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = dur, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(startDelay, StartOffsetType.Delay)
                ),
                label = "progress-$i"
            )

            val baseX = lerp(-200f, w + 200f, t)
            val baseY = lerp(yStart, yEnd, t)

            // 좌우 스웨이 속도도 조금 올림 (0.25f → 0.35f)
            val windX = drift + swayH * sin(2 * Math.PI * (t * 0.35f + p1)).toFloat()
            val flutterY = flutterV * sin(2 * Math.PI * (t * 0.8f + p2)).toFloat()

            val x = baseX + windX
            val y = baseY + flutterY

            val rotation = (t * 100f) + 8f * sin(2 * Math.PI * (t * 0.9f + p1)).toFloat()

            val sizePx = with(density) { sizeDp.toPx() }

            Image(
                painter = painterResource(leafRes),
                contentDescription = null,
                modifier = Modifier
                    .size(sizeDp)
                    .graphicsLayer {
                        translationX = x - sizePx / 2f
                        translationY = y - sizePx / 2f
                        rotationZ = rotation
                        alpha = 0.96f
                    }
            )
        }
    }
}
