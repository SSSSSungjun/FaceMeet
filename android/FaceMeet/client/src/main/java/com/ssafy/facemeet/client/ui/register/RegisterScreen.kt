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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.navigation.setting.RegisterMode
import com.ssafy.facemeet.client.ui.register.model.RegisterNaviEvent
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.client.ui.theme.ChosunSeirf
import com.ssafy.facemeet.client.ui.theme.FaceMeetTheme
import com.ssafy.facemeet.core.util.constant.CommonColor

private const val TAG = "RegisterScreen"

@Composable
fun RegisterScreen(
    mode: RegisterMode,
    toNext: () -> Unit = {},
    toBack: () -> Unit = {},
    toMap: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)

    LaunchedEffect(Unit) {
        Log.d("RegisterScreen", "navigationEvent: $navigationEvent")
        viewModel.updateAddressFromStore()
        viewModel.loadInitial(mode)
    }

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            RegisterNaviEvent.ToBack -> toBack()
            RegisterNaviEvent.ToCamera -> toNext()
            RegisterNaviEvent.ToMap -> toMap()

            else -> Log.d(TAG, "RegisterScreen: Unknown")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F3ED))
            .systemBarsPadding()
            .padding(10.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Log.d(TAG, "RegisterScreen: ${uiState.selectedAddress.toString()}")
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
                fontSize = 23.sp,
                fontFamily = ChosunCentennial,
                color = CommonColor.DarkBrown
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
                viewModel.navigateToNext()
                viewModel.submit(mode = mode)
            },
            onCancel = {
                viewModel.navigateToBack(mode)
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
            modifier = Modifier
                .padding(bottom = 5.dp),
            fontFamily = ChosunSeirf
        )
        Spacer(modifier = Modifier.padding(vertical = 2.dp))
        OutlinedTextField(
            value = nickname,
            onValueChange = {newNickname->
                if (newNickname.length <= 30) {
                    onNicknameChange(newNickname)
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = "닉네임을 입력하세요",
                    color = CommonColor.BeigeGray,
                )
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = CommonColor.Orange,
                unfocusedIndicatorColor = CommonColor.Brown500,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,

            )
    }
}

@Composable
fun InputAddress(
    selectedAddress: String,
    onLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Text(
            text = "주소",
            modifier = Modifier.padding(bottom = 5.dp),
            fontFamily = ChosunSeirf
        )
        Spacer(modifier = Modifier.padding(vertical = 2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLocationClick() } // 여기에 클릭 처리
        ) {
            OutlinedTextField(
                value = selectedAddress,
                onValueChange = {},
                readOnly = true,
                enabled = false, // 포커스도 꺼서 키보드가 안뜨게
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "주소를 선택하세요",
                        color = CommonColor.BeigeGray,
                    )
                },
                trailingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Location",
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = CommonColor.Brown500,
                    disabledContainerColor = Color.White,
                    disabledTextColor = CommonColor.Brown500
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
            )
        }


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
    ) {
        Text(
            text = "희망 매칭 연령대",
            modifier = Modifier.padding(bottom = 5.dp),
            fontFamily = ChosunSeirf
        )
        Spacer(modifier = Modifier.padding(vertical = 2.dp))
        CustomRangeSlider(
            value = selectedAgeRange.first.toFloat()..selectedAgeRange.last.toFloat(),
            onValueChange = { floatRange ->
                onAgeRangeChange(
                    floatRange.start.toInt(),
                    floatRange.endInclusive.toInt()
                )
            },
            valueRange = 20f..65f,
            thumbRadius = 11.dp,
            trackHeight = 7.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.padding(vertical = 10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${selectedAgeRange.first}세부터 ${selectedAgeRange.last}세까지",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                fontFamily = ChosunSeirf,
                color = CommonColor.Gray500
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

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    FaceMeetTheme {
        RegisterScreen(RegisterMode.REGISTER)
    }
}