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
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    LaunchedEffect(Unit) {viewModel.updateAddressFromStore() }

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
            )

            Spacer(modifier = Modifier.padding(20.dp))

            InputAgeRange(
                selectedAgeRange = uiState.selectedAgeRange,
                onAgeRangeChange = viewModel::updateAgeRange
            )

        }
        InputBtns(
            uiState.isValid()
        )
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
    viewModel: RegisterViewModel =hiltViewModel()
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
                        viewModel.navigateToMap()
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
fun InputAgeRange(
    selectedAgeRange: IntRange,
    onAgeRangeChange: (Int,Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "선호 나이 범위",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RangeSlider(
            value = selectedAgeRange.first.toFloat()..selectedAgeRange.last.toFloat(),
            onValueChange = { floatRange ->
                onAgeRangeChange(
                    floatRange.start.toInt(),
                    floatRange.endInclusive.toInt()
                )
            },
            valueRange = 20f..65f,
            steps = 44, //
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${selectedAgeRange.first}세",
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${selectedAgeRange.last}세",
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InputBtns(
    isEnabled: Boolean,
    viewModel: RegisterViewModel =hiltViewModel()
) {
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

