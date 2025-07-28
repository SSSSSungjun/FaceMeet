package com.ssafy.facemeet.client.ui.profile

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.camera.FaceAnalyzeViewModel
import com.ssafy.facemeet.client.util.hasWritePermission
import com.ssafy.facemeet.core.util.constant.CommonColor
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    viewModel: FaceAnalyzeViewModel = hiltViewModel(),
    onHome: () -> Unit,
    onMatching: () -> Unit,
    onRetry: () -> Unit
) {
    val result by viewModel.result.collectAsState()
    ProfileScreenContent(onHome, onMatching, onRetry)
}

@Composable
fun ProfileScreenContent(onHome: () -> Unit, onMatching: () -> Unit, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9F4))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TopBarSection(onHome = onHome)

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
            color = CommonColor.Gray500
        )

        HorizontalDivider(
            color = CommonColor.BeigeDark,
            thickness = 1.dp,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(23.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        DetailItem("얼굴형", "온화하고 지혜로운 눈매", R.drawable.nose)
        DetailItem("눈", "온화하고 지혜로운 눈매", R.drawable.nose)
        DetailItem("눈썹", "의지가 강한 형태", R.drawable.nose)
        DetailItem("코", "의지가 강한 형태", R.drawable.nose)
        DetailItem("턱", "따뜻한 성격을 나타내는 입술", R.drawable.nose)
        DetailItem("입", "따뜻한 성격을 나타내는 입술", R.drawable.nose)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(2.dp, color = CommonColor.Beige)
        ) {
            Surface(

                color = Color(0xFFFDFDFD),

                ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PersonalityDetail("✨ 성격", "배려심이 깊고 인간관계를 중시하는 성향입니다. 안정적이고 신뢰할 수 있는 파트너를 원합니다.")
                    Spacer(modifier = Modifier.height(20.dp))
                    PersonalityDetail(
                        "✨ 직업특성",
                        "배려심이 깊고 인간관계를 중시하는 성향입니다. 안정적이고 신뢰할 수 있는 파트너를 원합니다."
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PersonalityDetail(
                        "✨ 대인관계",
                        "배려심이 깊고 인간관계를 중시하는 성향입니다. 안정적이고 신뢰할 수 있는 파트너를 원합니다."
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PersonalityDetail(
                        "✨ 삶의방향",
                        "배려심이 깊고 인간관계를 중시하는 성향입니다. 안정적이고 신뢰할 수 있는 파트너를 원합니다."
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Color(0xFFE3D3C0)),
            color = Color.Transparent, // 중요! Surface 배경색 없애기
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFFCEE), Color(0xFFF4E5C9))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "종합 해석",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF8B5A2B),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "배려심이 깊고 인간관계를 중시하는 성향입니다. 안정적이고 신뢰할 수 있는 파트너를 원합니다.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF8B5A2B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
        Text(
            "관상 다시보기", fontSize = 13.sp, color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onRetry()
                }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "관상 프로필을 SNS에 공유해보세요!",
            color = Color(0xFFDC6E2F),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        shareRow()

        Spacer(modifier = Modifier.height(80.dp))
        Text(
            "나와 잘 맞는 사람은 누구일까?", fontSize = 13.sp, color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        MatchingStartButton(onMatching = onMatching)


    }
}

@Composable
fun TopBarSection(onHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Home",
            tint = Color(0xFF6B4C27),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable {
                    onHome()
                }
        )
        Text(
            text = "관상 분석 결과",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A3C2F),
            modifier = Modifier.align(Alignment.Center),
            fontSize = 24.sp
        )
    }
}


@Composable
fun DetailItem(
    title: String,
    desc: String,
    @DrawableRes iconResId: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFDFDFD),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommonColor.Gray900
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = desc, fontSize = 13.sp, color = CommonColor.Gray500)
        }
    }
}

@Composable
fun PersonalityDetail(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CommonColor.RedBrown
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = desc, fontSize = 13.sp, color = CommonColor.Gray900)
    }
}

@Composable
fun shareRow() {
    val context = LocalContext.current
    val activity = context as Activity

    val saveToGallery = remember {
        {
            captureComposableOffscreen(activity, content = {
                FaceResultCard(
                    title = "알 수 없상",
                    description = "배려심이 깊고 인간관계를 중시하는 성향입니다.",
                    faceImage = painterResource(id = R.drawable.temp_face)
                )
            }) { bitmap ->
                val success = saveBitmapToGallery(context, bitmap)
                Toast.makeText(
                    context,
                    if (success) "갤러리에 저장되었습니다!" else "저장에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            saveToGallery()
        } else {
            Toast.makeText(context, "저장 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val shareOptions = listOf(
        ShareOption(R.drawable.ic_share_kakao, "Kakao") {
            Log.d("SHARE", "Kakao 클릭됨")
        },
        ShareOption(R.drawable.ic_share_instagram, "Instagram") {
            Log.d("SHARE", "Instagram 클릭됨")
            captureComposableOffscreen(activity, content = {
                FaceResultCard(
                    title = "알 수 없상",
                    description = "배려심이 깊고 인간관계를 중시하는 성향입니다.",
                    faceImage = painterResource(id = R.drawable.temp_face)
                )
            }) { bitmap ->
                val file = saveBitmapToCacheFile(context, bitmap)
                shareToInstagramStory(context, file)
            }
        },
        ShareOption(R.drawable.ic_share_download, "Download") {
            if (hasWritePermission(context)) {
                saveToGallery()
            } else {
                permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    )

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        shareOptions.forEach { option ->
            IconButton(
                onClick = option.onClick,
                modifier = Modifier
                    .size(70.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = option.iconResId),
                    contentDescription = option.description,
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreenContent({}, {}, {})
    }
}

@Composable
fun MatchingStartButton(onMatching: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp) // 그림자 영역 확보
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x66D2691E),
                spotColor = Color(0x66D2691E)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFD2691E), Color(0xFFE88D4C))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .height(64.dp)
            .clickable {
                onMatching()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_heart), // 아이콘 리소스 등록 필요
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "매칭 시작하기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


data class ShareOption(
    val iconResId: Int,
    val description: String,
    val onClick: () -> Unit
)

fun createImageFile(context: Context): File? {
    val fileName = "shared_image_${System.currentTimeMillis()}.png"
    val file = File(context.cacheDir, fileName)

    return try {
        // 비트맵 생성 예시: 흰 배경에 텍스트 쓰기
        val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(CommonColor.Gray500.toArgb())
        val paint = Paint().apply {
            color = CommonColor.BeigeDark.toArgb()
            textSize = 40f
            isAntiAlias = true
        }
        canvas.drawText("FaceMeet 인스타 공유", 50f, 250f, paint)

        // 파일로 저장
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}