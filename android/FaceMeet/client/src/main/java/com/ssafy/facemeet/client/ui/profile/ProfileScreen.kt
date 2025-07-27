package com.ssafy.facemeet.client.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.camera.FaceAnalyzeViewModel
import com.ssafy.facemeet.core.util.constant.CommonColor


@Composable
fun ProfileScreen(
    viewModel: FaceAnalyzeViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()

    // 실제 분석 결과(result)를 넘기고 싶다면 ProfileScreenContent(result) 로 넘기기
    ProfileScreenContent()
}

@Composable
fun ProfileScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9F4))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()) // 스크롤 가능하게!
    ) {

        Row {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = Color(0xFF6B4C27),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = "관상 분석 결과",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF4A3C2F),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterVertically)
            )
        }


        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 이미지 자리 (리소스가 없을 땐 Box로 대체)
                Image(
                    painter = painterResource(id = R.drawable.temp_face),
                    contentDescription = "분석 결과 이미지",
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp)
                )
                Text("알수없상", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(17.dp))
                Text(
                    text = "츤데레 or 철벽? 알면 알수록 빠져드는 얼굴두줄두줄두줄두줄두줄두줄두줄",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )


            }
        }

        Spacer(modifier = Modifier.height(50.dp))
        Text(
            "세부 해석",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = CommonColor.gray500
        )

        HorizontalDivider(
            color = CommonColor.beige200,
            thickness = 1.dp,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(23.dp)
                .align(alignment = Alignment.CenterHorizontally),


            )

        Spacer(modifier = Modifier.height(8.dp))

        DetailItem("얼굴형", "온화하고 지혜로운 눈매")
        DetailItem("눈", "온화하고 지혜로운 눈매")
        DetailItem("👆눈썹", "의지가 강한 형태")
        DetailItem("👃코", "의지가 강한 형태")
        DetailItem("👄입", "따뜻한 성격을 나타내는 입술")

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFF5DC),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("✨ 총 해석", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "배려심이 깊고 인간관계를 중시하는 성향입니다.\n안정적이고 신뢰할 수 있는 파트너를 원합니다.",
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "관상 프로필을 SNS에 공유해보세요!",
            color = Color(0xFFDC6E2F),
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Chat, contentDescription = "Kakao")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Share, contentDescription = "Instagram")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("나와 잘 맞는 사람은 누구일까?", fontSize = 13.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE97E3A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("매칭 시작하기", color = Color.White)
        }
    }
}

@Composable
fun DetailItem(title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFDFDFD),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row (){  Image(
                painter = painterResource(id = R.drawable.nose),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommonColor.gray900
                )  }

            Spacer(modifier = Modifier.height(1.dp))
            Text(text = desc, fontSize = 13.sp, color = CommonColor.gray500)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreenContent()
    }
}
