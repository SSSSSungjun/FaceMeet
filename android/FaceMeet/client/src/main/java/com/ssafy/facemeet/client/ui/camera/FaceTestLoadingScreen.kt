package com.ssafy.facemeet.client.ui.camera

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import toMultipartBodyPart

@Composable
fun FaceTestLoadingScreen(
    cameraShotViewModel: CameraShotViewModel,
    analyzeViewModel: FaceAnalyzeViewModel,
    onNavigateToResult: () -> Unit
) {

    val isLoading by analyzeViewModel.isLoading.collectAsState()
    val result by analyzeViewModel.result.collectAsState()
    val error by analyzeViewModel.error.collectAsState()
    val context = LocalContext.current

    // 분석 요청 시작 (한 번만)
    LaunchedEffect(Unit) {
        val front = cameraShotViewModel.front.value
        val side = cameraShotViewModel.side.value

        if (front != null && side != null) {
            val frontPart = front.toMultipartBodyPart("image1")
            val sidePart = side.toMultipartBodyPart("side_image1")

            analyzeViewModel.analyzeFace(frontPart, sidePart)
        }
    }

    // ✅ 결과 도착 시 → 결과 화면으로 이동
    LaunchedEffect(result) {
        if (result != null) {
            onNavigateToResult()
        }
    }

    // ✅ 에러 발생 시 → 메시지 보여주기
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, "에러 발생: $it", Toast.LENGTH_SHORT).show()
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
                Text("에러가 발생했습니다.", fontSize = 16.sp)
            }

            else -> {
                Text("else")
                // 아무것도 안 보여줌 (혹시 result 도착 전 잠깐 비는 상태 방지용)
            }
        }
    }
}
