package com.ssafy.facemeet.client.ui.profile

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import java.io.File
import java.io.FileOutputStream

@Composable
fun FaceResultCard(
    name: String,
    title: String,
    description: String,
    faceImage: Painter
) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .background(Color(0xFFEAE3D8))
            .padding(horizontal = 20.dp, vertical = 70.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Text(
            "상견례", fontWeight = FontWeight.Bold, fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = ChosunCentennial,
            color = Color(0xFF8B5A2B)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "PlayStore에서 상견례를 다운받아 내 관상을 알아보세요.",
            color = Color(0xFFA28162),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = ChosunCentennial,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .height(500.dp)
                .padding(13.dp)
                .background(Color(0xFFEAE3D8))
                .clip(RoundedCornerShape(12.dp))
        ) {
            // 배경 이미지
            Image(
                painter = painterResource(id = R.drawable.bg_faced),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),

                ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = name, fontSize = 26.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = ChosunCentennial,
                            color = Color(0xFFB18444),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "의 관상은?", fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = ChosunCentennial,
                            color = Color(0xFFB18444),
                        )
                    }

                }

                Column {
                    Image(
                        painter = faceImage,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.74f) // 부모 가로의 70%
                            .aspectRatio(1f)    // 정사각형 유지
                            .align(Alignment.CenterHorizontally), // Column/Row라면 가운데 정렬
                        contentScale = ContentScale.Fit
                    )

                    Column {
                        Text(
                            text = title,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontFamily = ChosunCentennial,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF462C00)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = description,
                            color = Color(0xFF9E754E), fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            fontFamily = ChosunCentennial,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FaceResultCardPreview() {
    // 실제 이미지 리소스가 없을 경우 기본 placeholder 이미지로 대체 가능
    val sampleImage = painterResource(id = R.drawable.temp_face) // 🔁 리소스가 없다면 다른 drawable로 바꿔주세요

    FaceResultCard(
        name = "이름",
        title = "알 수 없상",
        description = "배려심이 깊고 인간관계를 중시하는 성향입니다. 안정적이고 신뢰할 수 있는 파트너를 원하는 얼굴",
        faceImage = sampleImage
    )
}

fun saveBitmapToCacheFile(context: Context, bitmap: Bitmap): File {
    // 저장할 파일 이름 (중복 방지)
    val fileName = "face_result_${System.currentTimeMillis()}.png"
    val file = File(context.cacheDir, fileName)

    // Bitmap → File 저장
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
    }

    return file
}

fun captureComposableOffscreen(
    activity: Activity,
    content: @Composable () -> Unit,
    onBitmapCaptured: (Bitmap) -> Unit
) {
    val composeView = ComposeView(activity).apply {
        setContent { content() }
        visibility = View.INVISIBLE
    }

    val decorView = activity.window.decorView as ViewGroup
    decorView.addView(composeView)

    // 👇 핵심: 측정 및 레이아웃 수동 지정
    composeView.measure(
        View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    composeView.layout(
        0,
        0,
        composeView.measuredWidth,
        composeView.measuredHeight
    )

    // 👇 바로 draw
    val bitmap = Bitmap.createBitmap(
        composeView.measuredWidth,
        composeView.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)

    // 👌 메모리 누수 방지
    decorView.removeView(composeView)

    onBitmapCaptured(bitmap)
}

