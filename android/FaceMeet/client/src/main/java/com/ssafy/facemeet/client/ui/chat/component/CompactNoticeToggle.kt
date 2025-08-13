package com.ssafy.facemeet.client.ui.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompactNoticeToggle(
    modifier: Modifier = Modifier,
    isNoticeOpen: Boolean, // uiState에서 받아옴
    onToggleNotice: () -> Unit, // viewModel::toggleNotice
    similar: Int = 89
) {
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedContent(
            targetState = isNoticeOpen,
            transitionSpec = {
                if (targetState) {
                    // 펼칠 때: 오른쪽에서 전체로 확장
                    slideInHorizontally(
                        initialOffsetX = { it / 2 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(200)
                            ) + fadeOut(animationSpec = tween(200))
                } else {
                    // 접을 때: 전체에서 오른쪽 작은 버튼으로
                    slideInHorizontally(
                        initialOffsetX = { -it / 2 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(200)
                            ) + fadeOut(animationSpec = tween(200))
                }
            },
            label = "notice_toggle"
        ) { isOpen ->
            if (isOpen) {
                // 펼쳐진 상태: 전체 너비 공지
                ExpandedNoticeCard(
                    similar = similar,
                    onClose = onToggleNotice // 닫기 버튼도 같은 함수 사용
                )
            } else {
                // 접힌 상태: 작은 버튼
                EnhancedCompactNoticeButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = onToggleNotice
                )
            }
        }
    }
}

@Composable
private fun ExpandedNoticeCard(
    similar: Int,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBFBFB)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {

        // 헤더 (제목 + 닫기 버튼)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "관상 궁합 ${similar}%로 매칭되었습니다 ✨\n" +
                            "프로필을 눌러 상대방의 관상을 살펴보세요",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // X 버튼
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

    }
}

@Composable
fun EnhancedCompactNoticeButton(
    modifier :Modifier =Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Card(
            onClick = onClick,
            modifier = modifier
                .size(44.dp)
                .padding(4.dp)
                .scale(pulse), // 펄스 효과
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF8F6D32)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "공지사항 보기",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompactNoticeTogglePreview() {
    CompactNoticeToggle(
        isNoticeOpen = true,
        onToggleNotice = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ExpandedNoticeCardPreview() {
    EnhancedCompactNoticeButton(
        onClick = {}
    )

}