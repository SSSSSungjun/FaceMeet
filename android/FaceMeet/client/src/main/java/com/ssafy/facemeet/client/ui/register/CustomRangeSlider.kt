package com.ssafy.facemeet.client.ui.register

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    thumbRadius: Dp = 8.dp,
    trackHeight: Dp = 4.dp,
    activeTrackColor: Color = Color(0xFF5B5141),
    inactiveTrackColor: Color = Color(0xFFAFAFAF),
    thumbColor: Color = Color(0xFF5B5141)
) {
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    var startThumbPosition by remember { mutableFloatStateOf(0f) }
    var endThumbPosition by remember { mutableFloatStateOf(0f) }
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    var isDraggingStart by remember { mutableStateOf(false) }
    var isDraggingEnd by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2 + 8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val distanceToStart = kotlin.math.abs(offset.x - startThumbPosition)
                        val distanceToEnd = kotlin.math.abs(offset.x - endThumbPosition)

                        if (distanceToStart < distanceToEnd) {
                            isDraggingStart = true
                        } else {
                            isDraggingEnd = true
                        }
                    },
                    onDragEnd = {
                        isDraggingStart = false
                        isDraggingEnd = false
                    }
                ) { _, dragAmount ->
                    if (isDraggingStart) {
                        startThumbPosition = (startThumbPosition + dragAmount.x).coerceIn(0f, endThumbPosition)
                    } else if (isDraggingEnd) {
                        endThumbPosition = (endThumbPosition + dragAmount.x).coerceIn(startThumbPosition, sliderWidth)
                    }

                    // 값 계산 및 콜백 호출
                    val startValue = valueRange.start + (startThumbPosition / sliderWidth) * (valueRange.endInclusive - valueRange.start)
                    val endValue = valueRange.start + (endThumbPosition / sliderWidth) * (valueRange.endInclusive - valueRange.start)

                    onValueChange(startValue..endValue)
                }
            }
    ) {
        sliderWidth = size.width
        val centerY = size.height / 2

        // 초기 위치 계산
        if (startThumbPosition == 0f && endThumbPosition == 0f) {
            startThumbPosition = ((value.start - valueRange.start) / (valueRange.endInclusive - valueRange.start)) * sliderWidth
            endThumbPosition = ((value.endInclusive - valueRange.start) / (valueRange.endInclusive - valueRange.start)) * sliderWidth
        }

        // 비활성 트랙 그리기
        drawRoundRect(
            color = inactiveTrackColor,
            topLeft = Offset(0f, centerY - trackHeightPx / 2),
            size = Size(sliderWidth, trackHeightPx),
            cornerRadius = CornerRadius(trackHeightPx / 2)
        )

        // 활성 트랙 그리기
        drawRoundRect(
            color = activeTrackColor,
            topLeft = Offset(startThumbPosition, centerY - trackHeightPx / 2),
            size = Size(endThumbPosition - startThumbPosition, trackHeightPx),
            cornerRadius = CornerRadius(trackHeightPx / 2)
        )

        // 시작 Thumb 그리기
        drawCircle(
            color = thumbColor,
            radius = thumbRadiusPx,
            center = Offset(startThumbPosition, centerY)
        )

        // 끝 Thumb 그리기
        drawCircle(
            color = thumbColor,
            radius = thumbRadiusPx,
            center = Offset(endThumbPosition, centerY)
        )
    }
}
