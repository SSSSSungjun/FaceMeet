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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 옆면(측면) 가이드:
 *  - 타원
 *  - 코 위치 가이드(라인 두 개로 간단히 표시)
 */
@Composable
fun FaceSideOverlay(
    countDown: Int?,
    guideText: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    scrimAlpha: Float = 0.6f
) {
    Box(modifier.fillMaxSize()) {

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
            val short = minOf(size.width, size.height)
            val faceW = short * 0.70f
            val faceH = faceW / 0.75f

            val cx = size.width * 0.5f
            val cy = size.height * 0.55f
            val ovalRect = Rect(
                left = cx - faceW / 2f,
                top = cy - faceH / 2f,
                right = cx + faceW / 2f,
                bottom = cy + faceH / 2f
            )

            // Scrim
            val outer = Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) }
            val inner = Path().apply { addOval(ovalRect) }
            val clipPath = Path().apply {
                op(outer, inner, PathOperation.Difference)
            }
            drawPath(path = clipPath, color = Color.Black.copy(alpha = scrimAlpha), style = Fill)

            // 타원 테두리
            drawOval(
                color = Color.White,
                topLeft = ovalRect.topLeft,
                size = ovalRect.size,
                style = Stroke(strokeWidth.toPx())
            )

            // 코 위치 가이드 (간단한 라인 2개)
            val noseX = cx + faceW * 0.2f
            val noseTop = cy - faceH * 0.1f
            val noseBottom = cy + faceH * 0.1f
            drawLine(
                color = Color.White,
                start = Offset(noseX, noseTop),
                end = Offset(noseX, noseBottom),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(noseX - faceW * 0.05f, cy),
                end = Offset(noseX + faceW * 0.05f, cy),
                strokeWidth = 2.dp.toPx()
            )
        }

        countDown?.let {
            Text(
                text = it.toString(),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 72.sp,
                color = Color.White
            )
        }
    }
}
