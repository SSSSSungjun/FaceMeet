package com.ssafy.facemeet.client.ui.register

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val LocalRegisterViewModel = compositionLocalOf<RegisterViewModel> {
    error("RegisterViewModel not provided")
}

private const val TAG = "RegisterScreen"

@Composable
fun RegisterScreen(
    onNavigateToNext: () -> Unit = {},
    onNavigateToBack: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            RegisterNaviEvent.ToCamera -> {
                onNavigateToNext()
            }

            RegisterNaviEvent.ToMap -> {
                onNavigateToMap()
            }

            RegisterNaviEvent.ToBack -> {
                onNavigateToBack()
            }

            null -> {
                Log.d(TAG, "NavigationEvent is null - staying on screen")
            }
        }
    }

    CompositionLocalProvider(LocalRegisterViewModel provides viewModel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "환영합니다",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.padding(40.dp))

                InputNickName(
                    nickname = uiState.nickname,
                    onNicknameChange = viewModel::updateNickname
                )

                Spacer(modifier = Modifier.padding(30.dp))

                InputAddress(
                    selectedAddress = uiState.selectedAddress,
                    hasLocation = uiState.hasLocation,
                    onNavigateToMap = viewModel::navigateToMap
                )

                // 디버깅용 위치 정보 표시
                if (uiState.hasLocation) {
                    Text(
                        text = "✅ 위치 선택 완료",
                        fontSize = 12.sp,
                        color = Color.Green,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            InputBtns(
                uiState.isValid()
            )
        }
    }
}

@Composable
fun InputNickName(
    nickname: String,
    onNicknameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "닉네임",
            modifier = Modifier.padding(bottom = 5.dp)
        )
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "닉네임을 입력하세요",
                    color = Color.Gray
                )
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF2196F3),
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Composable
fun InputAddress(
    selectedAddress: String,
    hasLocation: Boolean,
    onNavigateToMap: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = selectedAddress,
            onValueChange = {
            }, // 읽기 전용
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "주소를 선택하세요",
                    color = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = if (hasLocation) Color.Green else Color.Gray,
                    modifier = Modifier.clickable {
                        onNavigateToMap()
                    }
                )
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF2196F3),
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Composable
fun InputBtns(
    isEnabled: Boolean
) {
    val viewModel = LocalRegisterViewModel.current
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            onClick = {
                viewModel.navigateToCamera()
                viewModel.submitRegistration()
            },
            enabled = isEnabled
        ) {
            Text("확인")
        }

        Button(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            onClick = {
                viewModel.navigateToBack()
            }
        ) {
            Text("취소")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}