import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun FaceSideOverlay(
    countDown: Int?,
    guideText: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    scrimAlpha: Float = 0.6f
) {
    Box(modifier.fillMaxSize()) {

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

            // 얼굴 타원 테두리
            drawOval(
                color = Color.White,
                topLeft = ovalRect.topLeft,
                size = ovalRect.size,
                style = Stroke(strokeWidth.toPx())
            )

            // ⬇️ SVG path 변환한 코 윤곽선
            // 원본 뷰박스: width = 28, height = 60
            // 우리가 그릴 높이 기준: faceH * 0.8
            val scale = faceH * 0.8f / 150f
            val offsetX = cx - faceW * 0.35f
            val offsetY = cy - (60f * scale) / 3f

            val path = Path().apply {
                moveTo(offsetX + 22.4831f * scale, offsetY + 1f * scale)
                cubicTo(
                    offsetX + -1.00005f * scale, offsetY + 46f * scale,
                    offsetX + -10f * scale, offsetY + 41.5f * scale,
                    offsetX + 18f * scale, offsetY + 57f * scale
                )
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 4.dp.toPx())
            )
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
