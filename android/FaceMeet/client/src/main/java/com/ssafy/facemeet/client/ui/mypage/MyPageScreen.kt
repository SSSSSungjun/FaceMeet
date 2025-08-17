package com.ssafy.facemeet.client.ui.mypage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.ssafy.facemeet.core.util.constant.CommonColor

private const val TAG = "MyPageScreen"

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onMoveAfterLogout: () -> Unit,
    onMoveAfterWithdraw: () -> Unit,
    onModify: () -> Unit,
    onOpenBlocked: () -> Unit,

    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CommonColor.Beige100)
            .verticalScroll(rememberScrollState())
    ) {

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            viewModel.loadUserProfile()
            viewModel.event.collect { ev ->
                if (ev is MyPageViewModel.UiEvent.Toast) {
                    android.widget.Toast.makeText(
                        context,
                        ev.message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        LaunchedEffect(navigationEvent) {
            when (navigationEvent) {
                MyPageNaviEvent.ToLogout -> {
                    onMoveAfterLogout()
                }

                MyPageNaviEvent.ToModify -> {
                    onModify()
                }

                MyPageNaviEvent.ToWithdraw -> {
                    onMoveAfterWithdraw()
                }

                null -> {
                    Log.d(TAG, "MyPageScreen: null navi event")
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        PushNotificationSection(
            enabled = uiState.userInfo.isEventSubscribed,
            onToggle = viewModel::setMarketingAlarm
        )

        Spacer(modifier = Modifier.height(16.dp))

        MyInfoSection(
            uiState.userInfo,
            viewModel::navigateToModify
        )
        Spacer(modifier = Modifier.height(32.dp))

        ImportantSection(
            onBlockList = onOpenBlocked,
            onLogout = viewModel::navigateToLogout,
            onClickWithdraw = viewModel::clickWithdrawBtn,
            isShowDialog = uiState.isWithdrawBtnClicked,
            onDismissDialog = viewModel::dismissWithdrawDialog,
            onDelete = viewModel::navigateToWithdraw
        )

        Spacer(modifier = Modifier.height(50.dp))

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
                        checkedTrackColor = CommonColor.Orange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = CommonColor.Gray300,
                        uncheckedBorderColor = CommonColor.Gray300
                    ),
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = CommonColor.Gray500,
                    modifier = Modifier
                        .clickable { onModify() }
                        .padding(16.dp, 16.dp, 16.dp, 8.dp)
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
            color = CommonColor.Gray900
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = CommonColor.Gray400,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 250.dp),
            textAlign = TextAlign.End
        )

    }
}

@Composable
fun ImportantSection(
    onBlockList: () -> Unit = {},
    onLogout: () -> Unit,
    onClickWithdraw: () -> Unit,
    onDelete: () -> Unit,
    isShowDialog: Boolean,  // 탈퇴 다이얼로그 상태,
    onDismissDialog: () -> Unit // 다이얼로그 취소 처리
) {

    val context = LocalContext.current

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
                    text = "계정설정",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                )

            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ImportantRow(label = "내 차단목록 보기", onClick = onBlockList)

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ImportantRow(label = "로그아웃", onClick = {
                onLogout()
                Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            })

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            ImportantRow(label = "탈퇴하기", onClick = onClickWithdraw, color = CommonColor.Orange)

        }
    }

    if (isShowDialog) {
        WithdrawConfirmDialog(
            onConfirm = {
                onDelete()
                onDismissDialog()
            },
            onDismiss = onDismissDialog
        )
    }
}

@Composable
fun ImportantRow(label: String, onClick: () -> Unit, color: Color = CommonColor.Gray900) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFBBBBBB)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MaterialTheme {
        MyInfoSection(
            UserInfo(
                "윤성준",
                "1__________999@naver.com",
                "윤성주윤",
                "남",
                "ss",
                "1999-01-31",
                2,
                2,
                0.0,
                0.0,
                false
            )
        ) {}
    }
}

@Preview(showBackground = true, name = "BlockList - no badge")
@Composable
fun BlockListSectionPreview_NoCount() {
    MaterialTheme {
        ImportantSection(
            onBlockList = {},
            onLogout = {},
            onDismissDialog = {},
            isShowDialog = false,
            onClickWithdraw = {}, onDelete = {})
    }
}
