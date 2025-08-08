package com.ssafy.facemeet.client.ui.mypage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssafy.facemeet.client.ui.mypage.model.MyPageNaviEvent
import com.ssafy.facemeet.core.domain.model.UserInfo

private const val TAG = "MyPageScreen"

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onWithdraw: () -> Unit,
    onModify: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)

        LaunchedEffect(Unit) {
            viewModel.loadUserProfile()
        }

        LaunchedEffect(navigationEvent) {
            when (navigationEvent) {
                MyPageNaviEvent.ToLogout -> {
                    onLogout()
                }

                MyPageNaviEvent.ToModify -> {
                    onModify()
                }

                MyPageNaviEvent.ToWithdraw -> {
                    onWithdraw()
                }

                null -> {
                    Log.d(TAG, "MyPageScreen: null navi event")
                }
            }
        }

        TopBarSection(viewModel::navigateToLogout)
        ProfileImageSection()
        Spacer(modifier = Modifier.height(16.dp))

        PushNotificationSection(uiState.marketingAlarmEnabled) {
            viewModel::setMarketingAlarm
        }
        Spacer(modifier = Modifier.height(16.dp))

        MyInfoSection(
            uiState.userProfile,
            viewModel::navigateToModify
        )
        Spacer(modifier = Modifier.height(32.dp))

        WithdrawSection(
            viewModel::navigateToWithdraw,
            viewModel::clickWithdrawBtn,
            viewModel::dismissWithdrawDialog,
            uiState.isWithdrawBtnClicked
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSection(onLogout: () -> Unit = {}) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.clickable { onLogout() },
                text = "로그아웃",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun ProfileImageSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset((-4).dp, (-4).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun PushNotificationSection(
    enabled: Boolean = false,
    onToggle: (Boolean) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Text(
                text = "푸시 알림 설정",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "마케팅 알림 받기",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "신제품 마케팅 알림을 받아보세요",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2196F3),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun MyInfoSection(
    myData: UserInfo?,
    onModify: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "내 정보",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                )

                Text(
                    text = "수정하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .padding(16.dp, 16.dp, 16.dp, 8.dp)
                        .clickable { onModify() }
                )
            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ProfileInfoRow(label = "이메일", value = myData?.email ?: "")

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ProfileInfoRow(label = "이름", value = myData?.name ?: "")

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ProfileInfoRow(label = "닉네임", value = myData?.nickname ?: "")

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ProfileInfoRow(label = "생년월일", value = myData?.birth ?: "")

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ProfileInfoRow(label = "성별", value = myData?.getGenderLabel() ?: "")

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ProfileInfoRow(label = "주소", value = myData?.address ?: "")
        }
    }
}

@Composable
fun WithdrawSection(
    onWithdraw: () -> Unit,
    onClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    isShowDialog: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Gray
        )
    ) {
        Text(
            text = "탈퇴하기",
            fontSize = 14.sp,
        )
    }

    if (isShowDialog) {
        WithdrawConfirmDialog(
            onConfirm = {
                onWithdraw()
                onDismiss()
            },
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ProfileInfoRow(
    label: String = " ",
    value: String = " ",
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Gray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 250.dp),
            textAlign = TextAlign.End
        )

    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MaterialTheme {
        MyInfoSection(
            UserInfo("윤성준", "1__________999@naver.com", "윤성주윤", "남", "ss", "1999-01-31", 2, 2)
        ) {}
    }
}
