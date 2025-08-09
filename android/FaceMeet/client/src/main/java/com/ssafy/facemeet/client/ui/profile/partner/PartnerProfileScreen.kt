package com.ssafy.facemeet.client.ui.profile.partner

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.profile.DetailItem
import com.ssafy.facemeet.client.ui.profile.PersonalityDetail
import com.ssafy.facemeet.client.ui.profile.partner.dialog.BlockedDialog
import com.ssafy.facemeet.client.ui.profile.partner.dialog.ReportDialog
import com.ssafy.facemeet.client.ui.profile.partner.dialog.RoomExitDialog
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.core.data.remote.dto.response.ErrorResponse
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import com.ssafy.facemeet.core.util.constant.CommonColor
import kotlinx.coroutines.flow.collectLatest
import retrofit2.HttpException

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PartnerProfileScreen(
    roomId: Long,
    partnerId: Long,
    onBack: () -> Unit,
    onExitRoom: () -> Unit,
    viewModel: PartnerProfileViewModel = hiltViewModel()
) {
    Log.d("PartnerProfileScreen", "partnerId: $partnerId")


    val partnerFaceInfo by viewModel.partnerFaceInfo.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.exitRoomEvent.collectLatest {
            onExitRoom()
        }
    }
    LaunchedEffect(partnerId) {
        viewModel.loadPartnerFaceInfo(partnerId)
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("에러: $error")
            }
        }

        partnerFaceInfo != null -> {
            PartnerProfileContent(partnerFaceInfo!!, onBack, roomId = roomId, partnerId = partnerId)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PartnerProfileContent(
    result: PartnerFaceInfoResponse,
    onBack: () -> Unit,
    viewModel: PartnerProfileViewModel = hiltViewModel(),
    roomId: Long,
    partnerId: Long
) {
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val reportResult by viewModel.reportResult.collectAsState()

    LaunchedEffect(reportResult) {
        reportResult?.onSuccess {
            Toast.makeText(context, "신고가 완료되었습니다", Toast.LENGTH_SHORT).show()
        }?.onFailure { throwable ->
            val errorMessage = when (throwable) {
                is HttpException -> {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    try {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message
                    } catch (e: Exception) {
                        "오류 응답 파싱 실패"
                    }
                }

                else -> "알 수 없는 오류 발생"
            }

            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    ReportDialog(
        showDialog = showReportDialog,
        onDismiss = { showReportDialog = false },
        onReportSubmit = { categoryId, reason ->
            viewModel.sendReport(
                roomId = roomId,
                categoryId = categoryId,
                reportedId = partnerId,
                reason = reason
            )
            showReportDialog = false
        }
    )

    BlockedDialog(
        showDialog = showBlockedDialog,
        onDismiss = { showBlockedDialog = false },
        onConfirm = {
            viewModel.requestBlockUser(partnerId)
        }
    )

    RoomExitDialog(
        showDialog = showExitDialog,
        onDismiss = { showExitDialog = false },
        onConfirm = {
            viewModel.exitChatRoom(roomId)
        }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9F4))
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color(0xFF6B4C27),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBack() }
            )
            Text(
                text = "궁합 프로필",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF4A3C2F),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 24.sp,
                fontFamily = ChosunCentennial
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 얼굴 이미지
                Image(
                    painter = rememberAsyncImagePainter(model = result.img),
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .size(160.dp)
                        .padding(vertical = 16.dp)
                )

                // 닉네임 (나이)
                Text(
                    text = "${result.nickname} (${result.age}세)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = CommonColor.Gray900
                )

                // ● 접속 중
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (result.isOnline) Color(0xFF7DBE84) else Color.Gray,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (result.isOnline) "접속 중" else "오프라인",
                        fontSize = 13.sp,
                        color = if (result.isOnline) Color(0xFF8CB68B) else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 궁합도
                Text(
                    text = "${result.compatibility}%",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF5BB65F),
                                Color(0xFF66BB6A)
                            )
                        )
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "관상 궁합도",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 관상 유형 + 설명
                Surface(
                    color = Color(0xFFF9F9F9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = result.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CommonColor.Gray900
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = result.description,
                            fontSize = 13.sp,
                            color = CommonColor.Gray500,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(15.dp))


        BlockAndReportButtons(
            onBlock = { showBlockedDialog = true },
            onExit = { showExitDialog = true },
            onShowReportDialog = { showReportDialog = true },
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 얇은 구분선
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(25.dp)
                    .height(1.dp)
                    .background(Color(0xFFCECBBC))
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        DetailItem("얼굴형", result.faceShapeDesc, R.drawable.nose)
        DetailItem("눈", result.eyeDesc, R.drawable.nose)
        DetailItem("눈썹", result.eyebrowDesc, R.drawable.nose)
        DetailItem("코", result.noseDesc, R.drawable.nose)
        DetailItem("턱", result.chinDesc, R.drawable.nose)
        DetailItem("입", result.mouthDesc, R.drawable.nose)

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(2.dp, color = CommonColor.Beige)
        ) {
            Surface(

                color = Color(0xFFFDFDFD),

                ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PersonalityDetail("✨ 성격", result.personality)
                    PersonalityDetail("✨ 직업특성", result.careerTraits)
                    PersonalityDetail("✨ 대인관계", result.interpersonalRelationships)
                    PersonalityDetail("✨ 삶의방향", result.lifeDirection)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Color(0xFFE3D3C0)),
            color = Color.Transparent,
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
                        color = Color(0xFF8B5A2B),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        result.summaryAnalysis,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF8B5A2B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun BlockAndReportButtons(
    onBlock: () -> Unit,
    onExit: () -> Unit,
    onShowReportDialog: () -> Unit // 다이얼로그 상태 변경
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F8F4))
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            //채팅나가기
            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onExit() }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_exit_room), // ⛔ 아이콘 리소스
                    contentDescription = "채팅나가기",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "채팅나가기",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 차단하기
            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBlock() }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ban), // ⛔ 아이콘 리소스
                    contentDescription = "차단하기",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "차단하기",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 신고하기
            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onShowReportDialog() }
                    .clip(CircleShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_siren), // 🚨 아이콘 리소스
                    contentDescription = "신고하기",
                    tint = Color(0xFFD9534F), // 빨간색
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "신고하기",
                    fontSize = 14.sp,
                    color = Color(0xFFD9534F),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewReport() {
    ReportDialog(showDialog = true, onDismiss = {}, onReportSubmit = { _, _ -> })
}

@Preview(showBackground = true)
@Composable
fun PartnerProfilePreview() {
    val dummy = PartnerFaceInfoResponse(
        nickname = "소윤",
        isOnline = true,
        compatibility = 85,
        age = 26,
        img = "https://example.com/images/profile_sample.jpg",
        title = "따뜻한 리더형",
        description = "신뢰를 주는 분위기와 또렷한 이목구비를 지닌 사람입니다.",
        faceShapeDesc = "계란형 얼굴로 부드럽고 온화한 인상을 줍니다.",
        eyeDesc = "큼직하고 맑은 눈매로 진솔함을 나타냅니다.",
        eyebrowDesc = "진한 일자 눈썹은 결단력과 책임감을 의미합니다.",
        noseDesc = "오뚝한 코는 자존감이 높고 목표지향적인 성향을 의미합니다.",
        chinDesc = "둥글고 단단한 턱은 성실함과 배려심을 상징합니다.",
        mouthDesc = "입꼬리가 살짝 올라가 있어 긍정적이고 낙천적인 성격을 보여줍니다.",
        personality = "신중하고 따뜻한 성격으로, 타인의 의견을 잘 경청합니다.",
        careerTraits = "리더십이 강하고 협업을 중시하는 조직형 인재입니다.",
        interpersonalRelationships = "사람들과 조화를 이루며 긴 관계를 유지합니다.",
        lifeDirection = "안정과 조화를 중시하며, 꾸준한 성장을 추구합니다.",
        summaryAnalysis = "외모에서 드러나는 안정감과 신뢰성은 리더로서의 자질을 잘 나타냅니다. 주변 사람들과 조화를 이루며 함께 성장하는 타입입니다."
    )


    PartnerProfileContent(
        result = dummy, onBack = {},
        roomId = 0,
        partnerId = 0
    )
}
