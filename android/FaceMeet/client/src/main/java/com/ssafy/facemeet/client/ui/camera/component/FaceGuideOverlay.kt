package com.ssafy.facemeet.client.ui.camera.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.ml.FaceState
import kotlin.math.min

@Composable
fun FaceGuideOverlay(
    countDown: Int?,
    state: FaceState,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    scrimAlpha: Float = 0.6f
) {
    Box(modifier.fillMaxSize()) {

        val guideText = when (state) {
            FaceState.TOO_FAR -> "좀 더 가까이 와주세요"
            FaceState.TOO_CLOSE -> "조금만 멀어져주세요"
            FaceState.OUTSIDE -> "얼굴을 가이드에 맞춰주세요"
            FaceState.OK -> "좋아요! 그대로 유지하세요"
        }

        Text(
            text = guideText,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Color.White
        )

        Canvas(Modifier.fillMaxSize()) {
            val shortSide = min(size.width, size.height)

            // 얼굴 타원 비율: W:H = 3:4 → 0.75
            val faceWidth = shortSide * 0.70f
            val faceHeight = faceWidth / 0.75f

            val cx = size.width * 0.5f
            val cy = size.height * 0.55f

            val ovalRect = Rect(
                left = cx - faceWidth / 2f,
                top = cy - faceHeight / 2f,
                right = cx + faceWidth / 2f,
                bottom = cy + faceHeight / 2f
            )

            // 구멍난 스크림
            val hole = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addOval(ovalRect)
            }
            drawPath(hole, Color.Black.copy(alpha = scrimAlpha))

            // 흰색 테두리
            drawOval(
                color = Color.White,
                topLeft = Offset(ovalRect.left, ovalRect.top),
                size = Size(ovalRect.width, ovalRect.height),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        countDown?.let { sec ->
            Text(
                text = sec.toString(),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 72.sp,
                color = Color.White
            )
        }
    }
}
