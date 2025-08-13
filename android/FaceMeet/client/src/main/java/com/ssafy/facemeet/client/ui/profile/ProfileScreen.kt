package com.ssafy.facemeet.client.ui.profile

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.profile.ShareUtil.CircleShareButton
import com.ssafy.facemeet.client.ui.profile.ShareUtil.saveBitmapToGallery
import com.ssafy.facemeet.client.ui.profile.ShareUtil.shareImageWithText
import com.ssafy.facemeet.client.ui.profile.ShareUtil.shareToInstagramStory
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.client.ui.theme.FaceMeetTheme
import com.ssafy.facemeet.core.data.remote.dto.response.FaceInfoResponse
import com.ssafy.facemeet.core.util.constant.CommonColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onHome: () -> Unit,
    onMatching: () -> Unit,
    onRetry: () -> Unit,
) {
    val faceInfo by viewModel.faceInfo.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val tickets by viewModel.remainingMatchTickets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    when {
        isLoading -> {
            Text("불러오는 중...")
        }

        error != null -> {
            Text("에러: ${error}")
        }

        faceInfo != null -> {
            ProfileScreenContent(onHome, onMatching, onRetry, faceInfo!!, tickets = tickets)

        }
    }
}

@Composable
fun ProfileScreenContent(
    onHome: () -> Unit,
    onMatching: () -> Unit,
    onRetry: () -> Unit,
    faceInfo: FaceInfoResponse,
    tickets: Int?
) {

    var globalBusy by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9F4))
            .statusBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF9F4))
                .statusBarsPadding()
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
                        painter = rememberAsyncImagePainter(model = faceInfo.img),
                        contentDescription = "분석 결과 이미지",
                        modifier = Modifier
                            .size(240.dp)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(faceInfo.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(17.dp))
                    Text(
                        text = faceInfo.description,
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

            DetailItem(
                "얼굴형",
                faceInfo.faceShapeDesc,
                R.drawable.ic_shape
            )
            DetailItem("눈", faceInfo.eyeDesc, R.drawable.ic_eye)
            DetailItem(
                "눈썹",
                faceInfo.eyebrowDesc,
                R.drawable.ic_eyebrow
            )
            DetailItem("코", faceInfo.noseDesc, R.drawable.ic_nose)
            DetailItem("턱", faceInfo.chinDesc, R.drawable.ic_chin)
            DetailItem("입", faceInfo.mouthDesc, R.drawable.ic_lips)

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
                        PersonalityDetail("✨ 성격", faceInfo.personality)
                        Spacer(modifier = Modifier.height(20.dp))
                        PersonalityDetail(
                            "✨ 직업특성",
                            faceInfo.careerTraits
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        PersonalityDetail(
                            "✨ 대인관계",
                            faceInfo.interpersonalRelationships
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        PersonalityDetail(
                            "✨ 삶의방향",
                            faceInfo.lifeDirection
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
                            shape = RoundedCornerShape(12.dp)
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
                            faceInfo.summaryAnalysis,
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
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "관상 프로필을 SNS에 공유해보세요!",
                color = Color(0xFFDC6E2F),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            shareRow(faceInfo, onBusy = { isBusy -> globalBusy = isBusy })

            Spacer(modifier = Modifier.height(50.dp))
            Text(
                "나와 잘 맞는 사람은 누구일까?", fontSize = 13.sp, color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(30.dp))
            MatchingStartButton(onMatching = onMatching, buttonText = "매칭 시작하기", tickets = tickets)


        }


        GlobalLoadingOverlay(visible = globalBusy, text = "이미지 생성중…")
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
            fontSize = 24.sp,
            fontFamily = ChosunCentennial
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CommonColor.Gray900
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(text = desc, fontSize = 13.sp, color = CommonColor.Gray500)
        }
    }
}

@Composable
fun PersonalityDetail(title: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CommonColor.RedBrown,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = desc, fontSize = 13.sp, color = CommonColor.Gray900,
            lineHeight = 20.sp,
        )
    }
}

// 이미지 사전로드
suspend fun preloadFaceImage(context: Context, url: String): BitmapPainter? {
    val request = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false) // 캡처 시 필수
        .build()

    val drawable = context.imageLoader.execute(request).drawable
    return if (drawable != null) {
        val bitmap = (drawable as BitmapDrawable).bitmap
        BitmapPainter(bitmap.asImageBitmap())
    } else {
        null
    }
}

@Composable
fun shareRow(
    faceInfo: FaceInfoResponse,
    onBusy: (Boolean) -> Unit   // 전체 화면 오버레이 토글 콜백 (true: 보이기 / false: 숨기기)
) {
    val context = LocalContext.current
    val activity = (context as? Activity) ?: return
    val scope = rememberCoroutineScope()

    // 사전 로드된 이미지 (비동기 변동 없는 Painter)
    var painter by remember { mutableStateOf<BitmapPainter?>(null) }
    LaunchedEffect(faceInfo.img) {
        painter = preloadFaceImage(context, faceInfo.img)
    }

    // 캡처 대상 컴포저블
    val faceInfoShareCard: @Composable () -> Unit = {
        FaceResultCard(
            name = faceInfo.name,
            title = faceInfo.title,
            description = faceInfo.description,
            faceImage = painter ?: ColorPainter(Color.LightGray)
        )
    }

    // captureComposableOffscreen → suspend 래핑
    suspend fun captureAwait(): Bitmap = suspendCancellableCoroutine { cont ->
        captureComposableOffscreen(activity = activity, content = faceInfoShareCard) { bmp ->
            if (cont.isActive) cont.resume(bmp, onCancellation = null)
        }
    }

    // 공통 실행 유틸: 오버레이 표시 → 한 프레임 양보 → 작업 → 오버레이 해제
    fun runWithOverlay(block: suspend () -> Unit) {
        scope.launch {
            onBusy(true)
            withFrameNanos { } // 오버레이가 먼저 그려질 한 프레임 보장
            try {
                block()
            } catch (t: Throwable) {
                Log.e("ShareRow", "runWithOverlay error", t)
            } finally {
                onBusy(false)
            }
        }
    }

    // 액션들
    fun runDownload() = runWithOverlay {
        val bmp = captureAwait()                                      // 메인에서 캡처
        val ok = withContext(Dispatchers.IO) {                        // 저장은 IO
            saveBitmapToGallery(context, bmp)
        }
        Toast.makeText(
            context,
            if (ok) "갤러리에 저장되었습니다!" else "저장에 실패했습니다.",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun runInstagram() = runWithOverlay {
        val bmp = captureAwait()
        val file = withContext(Dispatchers.IO) {
            saveBitmapToCacheFile(context, bmp)
        }
        shareToInstagramStory(context, file)                          // 메인
    }

    fun runShare() = runWithOverlay {
        val bmp = captureAwait()
        val file = withContext(Dispatchers.IO) {
            saveBitmapToCacheFile(context, bmp)
        }
        shareImageWithText(
            context,
            file,
            "지금 관상 보러가기: https://play.google.com/store/apps/details?id=com.ssafy.facemeet"
        )
    }

    // 권한 런처 (승인 시 다운로드 실행)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) runDownload()
        else Toast.makeText(context, "저장 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(13.dp, Alignment.CenterHorizontally)
    ) {
        CircleShareButton(
            iconResId = R.drawable.ic_download,   // 벡터
            contentDescription = "Download",
            isPng = false,
            onClick = { runDownload() },
//             enabled = !globalBusy  // (옵션) 오버레이와 연동해서 비활성화하고 싶을 때
        )
        CircleShareButton(
            iconResId = R.drawable.ic_instragram, // PNG
            contentDescription = "Instagram",
            isPng = true,
            onClick = { runInstagram() }
        )
        CircleShareButton(
            iconResId = R.drawable.ic_share,      // 벡터
            contentDescription = "Share",
            isPng = false,
            onClick = { runShare() }
        )
    }

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreenContent(
            {}, {}, {}, FaceInfoResponse(
                faceId = 0,
                name = "",
                nickname = "",
                img = "",
                title = "",
                description = "",
                faceShapeDesc = "",
                eyeDesc = "",
                eyebrowDesc = "",
                noseDesc = "",
                chinDesc = "",
                mouthDesc = "",
                personality = "",
                careerTraits = "",
                interpersonalRelationships = "",
                lifeDirection = "",
                summaryAnalysis = "",
            ), 2
        )
    }
}


@Composable
fun MatchingStartButton(
    onMatching: () -> Unit,
    buttonText: String,
    tickets: Int?
) {
    val isDisabled = (tickets ?: 0) <= 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp) // 그림자 영역 확보
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x66D2691E),
                spotColor = Color(0x66D2691E)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDisabled) {
                        listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E)) // 회색
                    } else {
                        listOf(Color(0xFFD2691E), Color(0xFFE88D4C)) // 오렌지
                    }
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .height(64.dp)
            .clickable(enabled = !isDisabled) {
                onMatching()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = buttonText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDisabled) Color(0xFFEEEEEE) else Color.White
            )
        }
    }
}

@Composable
fun GlobalLoadingOverlay(
    visible: Boolean,
    text: String = "로딩중..."
) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)) // 연한 검정
            .clickable(                       // 터치 막기
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewShareRow() {
    FaceMeetTheme {
        shareRow(
            FaceInfoResponse(
                faceId = 0,
                name = "",
                nickname = "",
                img = "",
                title = "",
                description = "",
                faceShapeDesc = "",
                eyeDesc = "",
                eyebrowDesc = "",
                noseDesc = "",
                chinDesc = "",
                mouthDesc = "",
                personality = "",
                careerTraits = "",
                interpersonalRelationships = "",
                lifeDirection = "",
                summaryAnalysis = "",
            )
        ) { }
    }
}