package com.ssafy.facemeet.client.ui.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit = {},
    onLaunchCamera: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6ED))
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceEvenly, // ✅ 전체 세로 균등 간격
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 안내 텍스트
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "정면과 45도 각도로 한 번씩 촬영합니다.",
                fontSize = 20.sp,
                color = CommonColor.Gray900,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                textAlign = TextAlign.Start,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,

                )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "안경을 벗어주세요",
                fontSize = 18.sp,
                color = CommonColor.Brown500,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )// 캐릭터 이미지
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.camera_character),
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }


        // 예시 텍스트 + 사진
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "예시사진)",
                fontSize = 15.sp,
                color = CommonColor.Brown500,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaceExample(
                    image = painterResource(id = R.drawable.camera_example_front),
                    modifier = Modifier.weight(1f)
                )
                FaceExample(
                    image = painterResource(id = R.drawable.camera_example_side),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "사진은 저장하지 않습니다.",
                fontSize = 10.sp,
                color = CommonColor.BeigeGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            // 안내 텍스트
            Text(
                "준비되셨다면 시작버튼을 눌러주세요",
                fontSize = 14.sp,
                color = CommonColor.Brown100,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }


        // 시작 버튼
        Button(
            onClick = onLaunchCamera,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(10.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF7E6029),
                                Color(0xFF4D3713)
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "시작", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun FaceExample(image: Painter, modifier: Modifier = Modifier) {
    Image(
        painter = image,
        contentDescription = null,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    CameraScreen(
        onNavigateBack = {},
        onLaunchCamera = {}
    )
}