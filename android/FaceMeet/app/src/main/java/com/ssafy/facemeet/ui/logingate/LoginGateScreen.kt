@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.facemeet.navigation.gate

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.core.util.constant.CommonColor

private const val TAG = "LoginGateScreen"

@Composable
fun LoginGateScreen(
    onToRegister: () -> Unit,
    onToCamera: () -> Unit,
    onToMain: () -> Unit,
    onToStart: () -> Unit,
    viewModel: LoginGateViewModel = hiltViewModel()
) {
    // 1) 진입하자마자 분기 결정
    LaunchedEffect(Unit) { viewModel.decideNext() }

    // 2) 네비 이벤트 수집
    LaunchedEffect(Unit) {
        viewModel.nav.collect { nav ->
            Log.d(TAG, "LoginGateScreen: ${nav}")

            when (nav) {
                LoginGateViewModel.Nav.ToRegister -> onToRegister()
                LoginGateViewModel.Nav.ToCamera -> onToCamera()
                LoginGateViewModel.Nav.ToMain -> onToMain()
                LoginGateViewModel.Nav.ToStart -> onToStart()
                else -> {
                    Log.d(TAG, "LoginGateScreen: else")
                    onToStart()
                }
            }
        }
    }

    // 3) 로딩/에러 UI
    val ui by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (ui.loading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = CommonColor.Gray100)
                Spacer(Modifier.height(12.dp))
                Text("상태 확인 중…")
            }
        } else if (ui.error != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("네트워크 오류가 발생했어요.", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text(ui.error ?: "")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.retry() }) {
                    Text("다시 시도")
                }
            }
        } else {
            // 아주 잠깐 비는 상태: 로딩 표시 유지
            CircularProgressIndicator()
        }
    }
}
