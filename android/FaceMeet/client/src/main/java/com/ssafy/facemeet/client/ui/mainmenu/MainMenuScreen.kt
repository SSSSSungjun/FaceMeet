package com.ssafy.facemeet.client.ui.mainmenu

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.client.ui.theme.TitleTextStyle
import com.ssafy.facemeet.core.util.constant.CommonColor

private const val TAG = "MainMenuScreen"


@Composable
fun MainMenuScreen(
    onProfile: () -> Unit = {}, onMyPage: () -> Unit = {}, onNotification: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F2E9))  // 배경색 비슷하게 조정
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "상견례",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = ChosunCentennial,
                style = TitleTextStyle
            )
            IconButton(onClick = onNotification) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "알림 아이콘",
                    tint = CommonColor.Brown,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                )
            }


        }


        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0E3C3)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            onProfile()
                        }
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("얼굴 이미지", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "김철수님",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "알 수 없음",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }


                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = Color.Gray.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "매칭권 3",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        modifier = Modifier.clickable {
                            onMyPage()
                        },
                        text = "내 정보 보기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // 하단 버튼 2개
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* 인연 찾기 클릭 */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFE1F0) // 핑크톤
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💕", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("인연 찾기", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("관상 궁합으로 찾기", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = { /* 채팅 클릭 */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6EFE6) // 연한 그린톤
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💬", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("채팅", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("대화 목록", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    MainMenuScreen()
}


