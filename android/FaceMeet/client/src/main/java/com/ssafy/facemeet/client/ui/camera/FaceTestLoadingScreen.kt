package com.ssafy.facemeet.client.ui.camera

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.theme.Roboto
import com.ssafy.facemeet.core.util.constant.CommonColor
import kotlinx.coroutines.delay

@Composable
fun FaceTestLoadingScreen(
    cameraShotViewModel: CameraShotViewModel,
    analyzeViewModel: FaceAnalyzeViewModel,
    onNavigateToResult: () -> Unit,
    retry: () -> Unit,
    onCancel: () -> Unit,
) {

    val isLoading by analyzeViewModel.isLoading.collectAsState()
    val result by analyzeViewModel.result.collectAsState()
    val error by analyzeViewModel.error.collectAsState()

    fun tryAnalyze() {
        val front = cameraShotViewModel.front.value
        val side = cameraShotViewModel.side.value

        if (front != null && side != null) {
            val frontPart = front.toMultipartBodyPart("front_image")
            val sidePart = side.toMultipartBodyPart("side_image")

            analyzeViewModel.analyzeFace(frontPart, sidePart)
        }
    }

    // 분석 요청 시작 (한 번만)
    LaunchedEffect(Unit) {
        tryAnalyze()
    }


    // ✅ 결과 도착 시 → 결과 화면으로 이동
    LaunchedEffect(result) {
        if (result != null) {
            onNavigateToResult()
        }
    }

    // ✅ 상태에 따라 다른 UI 보여주기
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            isLoading -> {
                RotatingFaceLoadingScreen(onCancel)
            }

            error != null -> {
                CuteInlineError(
                    errorCode = error,
                    onRetry = { tryAnalyze() }
                )
            }

            else -> {
                // 아무것도 안 보여줌 (혹시 result 도착 전 잠깐 비는 상태 방지용)
            }
        }
    }

}

@Composable
fun RotatingFaceLoadingScreen(
    onCancel: () -> Unit
) {
    val images = listOf(
        R.drawable.character_look,   // "관상을 보아하니..."
        R.drawable.character_hmm,    // "흠... 그래... 알겠어..."
        R.drawable.character_wait    // "잠시만 기다려주게"
    )
    val fullTexts = listOf(
        "관상을 보아하니...",
        "흠... 그래... 알겠어...",
        "잠시만 기다려주게"
    )

    var currentIndex by remember { mutableStateOf(0) }
    var displayedText by remember { mutableStateOf("") }

    // 텍스트 타이핑 애니메이션 (1.5초 후에 시작)
    LaunchedEffect(currentIndex) {
        displayedText = ""
        val fullText = fullTexts[currentIndex]

        delay(800L) // 디졸브 절반 후 타이핑 시작

        for (i in fullText.indices) {
            displayedText = fullText.substring(0, i + 1)
            delay(100L) // 타이핑 속도
        }
    }

    // 3초마다 이미지 전환
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500L)
            currentIndex = (currentIndex + 1) % images.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDED3BA)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 타이핑 텍스트
            Text(
                text = displayedText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3D3D3D),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .alpha(0.9f)
            )

            // 이미지 디졸브 전환 (1초)
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith
                            fadeOut(animationSpec = tween(500))
                },
                label = "CharacterImage"
            ) { index ->
                Image(
                    painter = painterResource(id = images[index]),
                    contentDescription = null,
                    modifier = Modifier
                        .width(260.dp)
                        .heightIn(max = 400.dp)
                )
            }
        }

        // 하단 취소 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            TextButton(onClick = onCancel) {
                Text(
                    text = "취소",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        textDecoration = TextDecoration.Underline
                    ),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RotatingFaceLoadingScreenPreview() {
    RotatingFaceLoadingScreen(onCancel = {})
}


@Composable
fun CuteInlineError(
    errorCode: String? = null,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFE0B499))
            .padding(vertical = 28.dp),

        ) {

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "앗! 문제가 생겼어요",
                textAlign = TextAlign.Center,
                fontFamily = Roboto,
                fontSize = 18.sp,
                color = CommonColor.Brown500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            if (!errorCode.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "분석실패 : $errorCode",
                    textAlign = TextAlign.Center,
                    color = CommonColor.Brown500,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.image_error_sad),
            contentDescription = "미안한 표정의 캐릭터",
            modifier = Modifier.size(250.dp)
        )


        TextButton(onClick = onRetry) {
            Text(
                text = "다시 시도하기",
                color = Color(0xFFFFFFFF),
                fontSize = 16.sp,
                style = androidx.compose.ui.text.TextStyle(
                    textDecoration = TextDecoration.Underline
                ),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true) // 베이지 느낌 배경
@Composable
fun CuteInlineErrorPreview() {
    MaterialTheme {

        CuteInlineError(
            errorCode = "500",
            onRetry = {}
        )
    }
}