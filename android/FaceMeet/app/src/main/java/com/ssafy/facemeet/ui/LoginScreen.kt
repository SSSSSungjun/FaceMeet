package com.ssafy.facemeet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "로그인 화면",
                style = MaterialTheme.typography.headlineMedium
            )

            // 임시 버튼들 (실제로는 로그인 폼으로 교체)
            Button(
                onClick = {
                    // TODO: 실제 로그인 로직
                    onNavigateToMain()
                }
            ) {
                Text("일반 사용자 로그인")
            }

            Button(
                onClick = {
                    // TODO: 실제 관리자 로그인 로직
                    onNavigateToAdmin()
                }
            ) {
                Text("관리자 로그인")
            }
        }
    }
}
