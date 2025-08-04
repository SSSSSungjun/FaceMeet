package com.ssafy.facemeet.client.ui.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.theme.Roboto
import com.ssafy.facemeet.core.util.constant.CommonColor
import toMultipartBodyPart

@Composable
fun FaceTestLoadingScreen(
    cameraShotViewModel: CameraShotViewModel,
    analyzeViewModel: FaceAnalyzeViewModel,
    onNavigateToResult: () -> Unit,
    retry: () -> Unit,
) {

    val isLoading by analyzeViewModel.isLoading.collectAsState()
    val result by analyzeViewModel.result.collectAsState()
    val error by analyzeViewModel.error.collectAsState()

    fun tryAnalyze() {
        val front = cameraShotViewModel.front.value
        val side = cameraShotViewModel.side.value

        if (front != null && side != null) {
            val frontPart = front.toMultipartBodyPart("front_image1")
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("관상 분석 중입니다...", fontSize = 18.sp)
                }
            }

            error != null -> {
                CuteInlineError(
                    errorCode = error,
                    onRetry = { retry() }
                )
            }

            else -> {
                // 아무것도 안 보여줌 (혹시 result 도착 전 잠깐 비는 상태 방지용)
            }
        }
    }
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