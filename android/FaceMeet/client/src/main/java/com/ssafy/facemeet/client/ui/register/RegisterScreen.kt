package com.ssafy.facemeet.client.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    onNavigateToNext : () -> Unit = {},
    onNavigateToBack : () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 중앙 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textState = rememberSaveable { mutableStateOf("") }
            Text(
                text = "환영합니다",
                fontSize = 18.sp
            )

            Spacer(modifier=Modifier.padding(40.dp))
            InputNickName()
            Spacer(modifier=Modifier.padding(30.dp))
            InputAddress()
        }

        // 하단 버튼
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                onNavigateToNext()
            }
        ) {
            Text("확인")
        }
    }
}

@Composable
fun InputNickName() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        var searchText by rememberSaveable { mutableStateOf("") }

        Text(
            text="닉네임",
            modifier=Modifier.padding(bottom = 5.dp)
        )
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
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
fun InputAddress() {
    var searchText by rememberSaveable { mutableStateOf("경상북도 진평동") }
    var selectedProvince by rememberSaveable { mutableStateOf("경상북도") }
    var selectedCity by rememberSaveable { mutableStateOf("구미시") }
    var selectedDistrict by rememberSaveable { mutableStateOf("진평동") }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 10.dp)
    ) {
        // 검색창
        InputAddressEditText(
            searchText = searchText,
            onSearchTextChange = { searchText = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 필터 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownButton(
                modifier = Modifier.weight(1f),
                value = selectedProvince,
                onValueChange = { selectedProvince = it },
                options = listOf("경상북도", "경상남도", "전라북도", "전라남도", "충청북도")
            )

            DropdownButton(
                modifier = Modifier.weight(1f),
                value = selectedCity,
                onValueChange = { selectedCity = it },
                options = listOf("구미시", "대구시", "부산시", "울산시", "포항시")
            )

            DropdownButton(
                modifier = Modifier.weight(1f),
                value = selectedDistrict,
                onValueChange = { selectedDistrict = it },
                options = listOf("진평동", "송정동", "도량동", "임수동", "형곡동")
            )
        }
    }
}

@Composable
fun InputAddressEditText(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "경상북도 진평동",
                color = Color.Gray
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color.Gray
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownButton(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF2196F3),
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 14.sp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}