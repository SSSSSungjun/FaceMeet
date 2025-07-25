package com.ssafy.facemeet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.R
import com.ssafy.facemeet.core.util.constant.CommonColor
import com.ssafy.facemeet.ui.web.WebLoginScreen



@Composable
fun LoginScreen(
    onNavigateToClient: () -> Unit, //일단 카카오만
    onNavigateToAdmin: () -> Unit
) {
    var socialState by rememberSaveable { mutableStateOf(SocialLoginProvider.NONE) }

    if (socialState != SocialLoginProvider.NONE) {

        WebLoginScreen(socialState) {
            socialState = SocialLoginProvider.NONE
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = CommonColor.background1),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "상견례",
                color = Color.White,
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.padding(20.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "로고",
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.padding(20.dp))

            Text(
                text = "로그인하고 AI로 분석한 \n 내 관상으로 프로필을 만들어보세요!",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.padding(30.dp))

            Button(
                modifier = Modifier
                    .width(340.dp)
                    .background(
                        color = Color(0xFFFFE812),
                        shape = RoundedCornerShape(5.dp)
                    ),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    socialState = SocialLoginProvider.KAKAO
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                elevation = null,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.kakao_logo),
                        contentDescription = "카카오 로그인",
                        modifier = Modifier
                            .size(33.dp)
                            .align(Alignment.CenterStart)
                    )
                    Text(
                        text = "카카오 로그인",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.padding(10.dp))

            Button(
                modifier = Modifier
                    .width(340.dp)
                    .background(
                        color = Color(0xFF03C75A),
                        shape = RoundedCornerShape(5.dp)
                    ),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    socialState = SocialLoginProvider.NAVER
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                elevation = null,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.naver_logo),
                        contentDescription = "네이버 로그인",
                        modifier = Modifier
                            .size(33.dp)
                            .align(Alignment.CenterStart)
                    )
                    Text(
                        text = "네이버 로그인",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

        }
    }

}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen({}, {})
}

enum class SocialLoginProvider {
    KAKAO,
    NAVER,
    NONE
}
