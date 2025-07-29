package com.ssafy.facemeet.client.ui.register

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    var nickname by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf("") }
    var hasLocation by remember { mutableStateOf(false) }

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

            PreviewInputNickName(
                nickname = nickname,
                onNicknameChange = { nickname = it }
            )

            Spacer(modifier = Modifier.padding(30.dp))

            PreviewInputAddress(
                selectedAddress = selectedAddress,
                hasLocation = hasLocation,
                onLocationClick = {
                    hasLocation = !hasLocation
                    selectedAddress = if (hasLocation) "서울시 강남구 테헤란로 123" else ""
                }
            )

            val ageRange: IntRange = 20..30
            RangeSlider(
                value = ageRange.first.toFloat()..ageRange.last.toFloat(),
                onValueChange = { },
                valueRange = 18f..65f,
                steps = 46 // 18~65 사이 각 나이별 스텝
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${ageRange.start.toInt()}세",
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${ageRange.endInclusive.toInt()}세",
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Medium
                )
            }

        }

        PreviewInputBtns(
            isEnabled = nickname.isNotBlank() && selectedAddress.isNotBlank(),
            onConfirm = { /* 확인 버튼 클릭 */ },
            onCancel = { /* 취소 버튼 클릭 */ }
        )
    }
}

@Composable
fun PreviewInputNickName(
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
fun PreviewInputAddress(
    selectedAddress: String,
    hasLocation: Boolean,
    onLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
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
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = if (hasLocation) Color.Green else Color.Gray,
                    modifier = Modifier.clickable { onLocationClick() }
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
fun PreviewInputBtns(
    isEnabled: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            onClick = onConfirm,
            enabled = isEnabled
        ) {
            Text("확인")
        }

        Button(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            onClick = onCancel
        ) {
            Text("취소")
        }
    }
}

