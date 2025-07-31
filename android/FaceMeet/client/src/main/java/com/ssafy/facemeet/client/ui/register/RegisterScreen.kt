package com.ssafy.facemeet.client.ui.register

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.register.model.RegisterNaviEvent

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
    LaunchedEffect(Unit) {
        Log.d("RegisterScreen", "navigationEvent: $navigationEvent")
        viewModel.updateAddressFromStore()
    }

    LaunchedEffect(navigationEvent) {
        when(navigationEvent){
            RegisterNaviEvent.ToBack -> onNavigateToBack()
            RegisterNaviEvent.ToCamera -> onNavigateToNext()
            RegisterNaviEvent.ToMap -> onNavigateToMap()
            else -> Log.d(TAG, "RegisterScreen: Unknown")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F3ED))
            .systemBarsPadding(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .wrapContentHeight()
                .align(Alignment.Center)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "정보를 입력해주세요",
                fontSize = 23.sp
            )

            Spacer(modifier = Modifier.padding(40.dp))

            InputNickName(
                nickname = uiState.nickname,
                onNicknameChange = viewModel::updateNickname
            )

            Spacer(modifier = Modifier.padding(20.dp))

            InputAddress(
                selectedAddress = uiState.selectedAddress,
                onLocationClick = { viewModel.navigateToMap() }
            )

            Spacer(modifier = Modifier.padding(20.dp))

            InputAgeRange(
                selectedAgeRange = uiState.selectedAgeRange,
                onAgeRangeChange = viewModel::updateAgeRange
            )
        }

        InputBtns(
            isEnabled = uiState.isValid(),
            onConfirm = {
                viewModel.navigateToCamera()
                viewModel.submitRegistration()
            },
            onCancel = {
                viewModel.navigateToBack()
            }
        )
    }
}

@Composable
fun InputNickName(
    nickname: String,
    onNicknameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
    onLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "주소",
            modifier = Modifier.padding(bottom = 5.dp)
        )
        OutlinedTextField(
            value = selectedAddress,
            onValueChange = { }, // 읽기 전용
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "주소를 선택하세요",
                    color = Color.Gray
                )
            },
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.location_icon),
                    contentDescription = "Location",
                    modifier = Modifier
                        .clickable { onLocationClick() }
                        .size(24.dp)
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
    onAgeRangeChange: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = "희망 매칭 연령대",
            modifier = Modifier.padding(bottom = 5.dp)
        )

        CustomRangeSlider(
            value = selectedAgeRange.first.toFloat()..selectedAgeRange.last.toFloat(),
            onValueChange = { floatRange ->
                onAgeRangeChange(
                    floatRange.start.toInt(),
                    floatRange.endInclusive.toInt()
                )
            },
            valueRange = 20f..65f,
            thumbRadius = 6.dp, // 작은 Thumb
            trackHeight = 6.dp, // 얇은 트랙
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
        )

        Spacer(modifier = Modifier.padding(vertical = 10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${selectedAgeRange.first}세부터 ${selectedAgeRange.last}세까지",
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun InputBtns(
    isEnabled: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
            onClick = onConfirm,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5B5141),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD6D6D6),
                disabledContentColor = Color(0xFF898989)
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "확인",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(5.dp)
            )
        }

        Spacer(modifier = Modifier.padding(10.dp))

        Button(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5B5141),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD6D6D6),
                disabledContentColor = Color(0xFFAAAAAA)
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "취소",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}