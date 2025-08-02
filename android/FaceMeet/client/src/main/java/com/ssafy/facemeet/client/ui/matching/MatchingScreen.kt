package com.ssafy.facemeet.client.ui.matching

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.profile.MatchingStartButton
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun MatchingScreen(
    onStartMatching: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = CommonColor.Beige200)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth().padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "관상이 말해주는 궁합, AI와 데이터로\n당신에게 가장 잘 맞는 얼굴을 찾아 드릴게요.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                color = CommonColor.Brown,
                modifier = Modifier,
                textAlign = TextAlign.Center
            )

            // 이미지 (예: R.drawable.match_image 는 이미지 리소스에 따라 수정 필요)
            Image(
                painter = painterResource(id = R.drawable.ic_matching),
                contentDescription = "매칭 이미지",
                modifier = Modifier
                    .height(230.dp)
            )



        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,) {
            // 매칭권 개수 텍스트
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)){
                Text(
                    text = "매칭권", // 추후 상태 기반으로 교체 가능
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommonColor.Gray300, // 갈색 계열,

                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = " 3", // 추후 상태 기반으로 교체 가능
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommonColor.Yellow, // 갈색 계열
                )
            }
            // 매칭 버튼
            MatchingStartButton(onMatching = onStartMatching, buttonText = "매칭권 사용")

        }

    }
}

@Preview(showBackground = true)
@Composable
fun MatchingScreenPreview() {
    // 테마가 있다면 여기에 감싸주세요 (예: MyAppTheme { ... })
    MatchingScreen(
        onStartMatching = { /* 프리뷰용 - 클릭 무시 */ }
    )
}
