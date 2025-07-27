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

@Composable
fun FaceTestLoadingScreen(
    viewModel: FaceAnalyzeViewModel,
    onNavigateToResult: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.result.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

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

    // ✅ 로딩 화면 UI
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("관상 분석 중입니다...", fontSize = 18.sp)
        }
    }
}