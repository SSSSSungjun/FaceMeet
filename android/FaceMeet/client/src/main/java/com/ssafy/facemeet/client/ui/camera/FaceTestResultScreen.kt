package com.ssafy.facemeet.client.ui.camera

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FaceTestResultScreen(
    viewModel: FaceAnalyzeViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("관상 분석 결과", fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        Text(result ?: "결과 없음")
    }
}
