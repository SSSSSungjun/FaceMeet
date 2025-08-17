package com.ssafy.facemeet.client.ui.register

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun CustomRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    thumbRadius: Dp,
    trackHeight: Dp,
    activeTrackColor: Color = CommonColor.DarkBrown,
    inactiveTrackColor: Color = CommonColor.BeigeDark,
    thumbColor: Color = CommonColor.DarkBrown
) {
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    var startThumbPx by remember { mutableFloatStateOf(0f) }
    var endThumbPx by remember { mutableFloatStateOf(0f) }
    var sliderWidth by remember { mutableFloatStateOf(0f) }

    var isDraggingStart by remember { mutableStateOf(false) }
    var isDraggingEnd by remember { mutableStateOf(false) }

    fun valueToPx(v: Float): Float {
        if (sliderWidth == 0f) return 0f
        val clamped = v.coerceIn(valueRange.start, valueRange.endInclusive)
        val ratio = (clamped - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        return ratio * sliderWidth
    }

    fun pxToValue(px: Float): Float {
        if (sliderWidth == 0f) return value.start
        val ratio = (px / sliderWidth).coerceIn(0f, 1f)
        return valueRange.start + ratio * (valueRange.endInclusive - valueRange.start)
    }

    // 외부 value가 바뀌면(프리필 등), 드래그 중이 아닐 때만 썸 위치 동기화
    LaunchedEffect(value, sliderWidth, valueRange) {
        if (sliderWidth > 0f && !isDraggingStart && !isDraggingEnd) {
            startThumbPx = valueToPx(value.start)
            endThumbPx = valueToPx(value.endInclusive).coerceAtLeast(startThumbPx)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2 + 8.dp)
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            // ❌ pointerInput(value, ...) 금지
            // ✅ 고정 키 사용해서 제스처 중 재생성 방지
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val ds = kotlin.math.abs(offset.x - startThumbPx)
                        val de = kotlin.math.abs(offset.x - endThumbPx)
                        isDraggingStart = ds <= de
                        isDraggingEnd = !isDraggingStart
                    },
                    onDragEnd = {
                        isDraggingStart = false
                        isDraggingEnd = false
                    }
                ) { change, drag ->
                    if (isDraggingStart) {
                        startThumbPx = (startThumbPx + drag.x).coerceIn(0f, endThumbPx)
                    } else if (isDraggingEnd) {
                        endThumbPx = (endThumbPx + drag.x).coerceIn(startThumbPx, sliderWidth)
                    }
                    onValueChange(pxToValue(startThumbPx)..pxToValue(endThumbPx))
                    change.consume()
                }
            }
    ) {
        val centerY = size.height / 2

        // 비활성 트랙
        drawRoundRect(
            color = inactiveTrackColor,
            topLeft = Offset(0f, centerY - trackHeightPx / 2),
            size = Size(sliderWidth, trackHeightPx),
            cornerRadius = CornerRadius(trackHeightPx / 2)
        )

        // 활성 트랙 (두 썸 사이)
        val activeWidth = (endThumbPx - startThumbPx).coerceAtLeast(0f)
        drawRoundRect(
            color = activeTrackColor,
            topLeft = Offset(startThumbPx, centerY - trackHeightPx / 2),
            size = Size(activeWidth, trackHeightPx),
            cornerRadius = CornerRadius(trackHeightPx / 2)
        )

        // 썸
        drawCircle(thumbColor, thumbRadiusPx, Offset(startThumbPx, centerY))
        drawCircle(thumbColor, thumbRadiusPx, Offset(endThumbPx, centerY))
    }
}
