package com.ssafy.facemeet.client.ui.mainmenu

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.client.ui.theme.ChosunSeirf
import com.ssafy.facemeet.client.ui.theme.FaceMeetTheme
import com.ssafy.facemeet.client.ui.theme.TitleTextStyle
import com.ssafy.facemeet.core.util.constant.CommonColor

private const val TAG = "MainMenuScreen"


@Composable
fun MainMenuScreen(
    onProfile: () -> Unit = {}, onNotification: () -> Unit = {}, onMatch: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F2E9))  // 배경색 비슷하게 조정
            .padding(start = 16.dp, end = 16.dp, bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "상견례",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                fontFamily = ChosunCentennial,
                style = TitleTextStyle,
                modifier = Modifier.padding(start = 6.dp)
            )
            IconButton(onClick = onNotification) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "알림 아이콘",
                    tint = CommonColor.Brown500,
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.padding(4.dp))

        ProfileCardWithBackground(onProfile)
        Spacer(modifier = Modifier.padding(10.dp))

        // 하단 버튼 2개
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onMatch() },
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color(0x33000000), // 연한 그림자 (20% 불투명도)
                        spotColor = Color(0x33000000)     // 같이 써줘야 효과 있음
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp) // 원하는 크기로 조정
                            .background(
                                Color(0xFFFCE4EC),
                                shape = RoundedCornerShape(12.dp)
                            ), // 연한 핑크 배경
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_heart_pink),
                            contentDescription = "인연 찾기",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "인연 찾기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CommonColor.Gray900
                    )
                    Spacer(modifier = Modifier.padding(1.dp))
                    Text("관상 궁합으로 찾기", fontSize = 12.sp, color = CommonColor.Gray300)
                }
            }
            Button(
                onClick = { /* 채팅 클릭 */ },
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color(0x33000000), // 연한 그림자 (20% 불투명도)
                        spotColor = Color(0x33000000)     // 같이 써줘야 효과 있음
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFFE0F2F1), // 연한 민트 배경
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat),
                            contentDescription = "채팅",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "채팅",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CommonColor.Gray900
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text("대화 목록", fontSize = 12.sp, color = CommonColor.Gray300)
                }
            }

        }
    }
}

@Composable
fun ProfileCardWithBackground(
    onProfile: () -> Unit,
    viewModel: MainMenuViewModel = hiltViewModel()
) {

    val ui by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHome() }

    when {
        ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        ui.error != null -> Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("불러오기에 실패했어요.\n${ui.error}")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.loadHome() }) { Text("다시 시도") }
        }

        else -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // 배경 이미지
                Image(
                    painter = painterResource(id = R.drawable.bg_face),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // 카드 내용
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProfile() }
                                .padding(horizontal = 32.dp, vertical = 40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = ui.img), // ← 여기에 실제 리소스 ID 입력
                                    contentDescription = "사용자 얼굴",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = ui.nickname,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                fontFamily = ChosunCentennial,
                                color = CommonColor.BrownGray900
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = ui.title,
                                fontSize = 16.sp,
                                color = CommonColor.BrownGray600,
                                fontFamily = ChosunSeirf
                            )
                        }

                        Divider(
                            color = CommonColor.Gray200,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 매칭권 3 영역 (weight 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {}
                                    .padding(top = 12.dp, bottom = 22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "매칭권 ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = CommonColor.BrownGray600
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = ui.remainingMatchTickets.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = CommonColor.BrownGray600
                                    )
                                }
                            }

                            // 세로선
                            Divider(
                                color = CommonColor.Gray200,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(top = 12.dp, bottom = 22.dp)
                                    .width(1.dp)
                            )

                            // 내 정보 보기 영역 (weight 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onProfile() }
                                    .padding(top = 12.dp, bottom = 22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "내 관상 보기",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = CommonColor.BrownGray600
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenSucreenPreview() {
    FaceMeetTheme {
        MainMenuScreen()
    }
}


