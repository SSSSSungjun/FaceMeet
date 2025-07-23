@file:OptIn(
    androidx.camera.core.ExperimentalGetImage::class
)

package com.ssafy.facemeet.client.ml

import android.content.Context
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.State
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.ssafy.facemeet.client.ui.camera.CaptureMode
import kotlin.math.ceil
import kotlin.math.pow

private const val HOLD_MS = 1_500L
private const val MIN_FACE_H_RATIO = 0.18f
private const val MAX_FACE_H_RATIO = 0.60f
private const val OK_STREAK_NEED = 3
private const val OVAL_PADDING = 0.12f

class FaceAnalyzer(
    private val context: Context,
    private val controller: LifecycleCameraController,
    private val previewViewState: State<PreviewView?>,
    private val ovalSpec: FaceOvalSpec,
    private val mode: CaptureMode,                 // ★ 추가
    private val onHoldDone: () -> Unit,
    private val onProgress: (Int) -> Unit,
    private val onStateChanged: (FaceState) -> Unit
) {

    private val detector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .enableTracking()
                .build()
        )
    }

    private var startAt: Long? = null
    private var fired = false
    private var lastReported = -1
    private var lastState: FaceState? = null
    private var okStreak = 0

    fun bind() {
        val executor = ContextCompat.getMainExecutor(context)

        controller.setImageAnalysisAnalyzer(executor) { imageProxy ->
            if (fired) {
                imageProxy.close(); return@setImageAnalysisAnalyzer
            }

            @Suppress("UnsafeOptInUsageError")
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close(); return@setImageAnalysisAnalyzer
            }

            val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val preview = previewViewState.value ?: run {
                imageProxy.close(); return@setImageAnalysisAnalyzer
            }

            val imgW = imageProxy.width.toFloat()
            val imgH = imageProxy.height.toFloat()
            val viewW = preview.width.toFloat().coerceAtLeast(1f)
            val viewH = preview.height.toFloat().coerceAtLeast(1f)
            val mirror = controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
            val sd = calcFillCenter(imgW, imgH, viewW, viewH, mirror)

            detector.process(input)
                .addOnSuccessListener { faces ->
                    var state = FaceState.OUTSIDE
                    var ok = false

                    faces.forEach { face ->
                        // 정면/옆면 체크 분기
                        val passPose =
                            if (mode == CaptureMode.FRONT) face.isFrontPose()
                            else face.isSidePose()

                        if (!passPose) return@forEach

                        val rect = face.boundingBox.toRectF()
                        rect.mapToPreview(sd, viewW)

                        val cx = rect.centerX() / viewW
                        val cy = rect.centerY() / viewH
                        val hRatio = rect.height() / viewH

                        state = when {
                            hRatio < MIN_FACE_H_RATIO -> FaceState.TOO_FAR
                            hRatio > MAX_FACE_H_RATIO -> FaceState.TOO_CLOSE
                            else -> {
                                val a = (ovalSpec.widthRatio * (1f + OVAL_PADDING)) / 2f
                                val b = (ovalSpec.heightRatio * (1f + OVAL_PADDING)) / 2f
                                val inside = (((cx - ovalSpec.centerXRatio) / a).pow(2) +
                                        ((cy - ovalSpec.centerYRatio) / b).pow(2)) <= 1f
                                if (inside) FaceState.OK else FaceState.OUTSIDE
                            }
                        }

                        if (state == FaceState.OK) {
                            ok = true
                            return@forEach
                        }
                    }

                    if (state != lastState) {
                        lastState = state
                        onStateChanged(state)
                    }

                    val now = System.currentTimeMillis()
                    if (ok) {
                        okStreak++
                        if (okStreak >= OK_STREAK_NEED) {
                            if (startAt == null) startAt = now
                            val elapsed = now - (startAt ?: now)
                            val remainMs = (HOLD_MS - elapsed).coerceAtLeast(0)
                            val secLeft = ceil(remainMs / 1000.0).toInt().coerceAtLeast(0)

                            if (secLeft != lastReported && !fired) {
                                lastReported = secLeft
                                if (secLeft in 1..3) onProgress(secLeft)
                            }
                            if (!fired && elapsed >= HOLD_MS) {
                                fired = true
                                onHoldDone()
                            }
                        }
                    } else {
                        okStreak = 0
                        startAt = null
                        lastReported = -1
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    fun clear() {
        controller.clearImageAnalysisAnalyzer()
        detector.close()
    }
}

/* ========= Pose 판단 간단 버전 ========= */
private fun com.google.mlkit.vision.face.Face.isFrontPose(): Boolean {
    // Euler Y, Z가 너무 크지 않으면 정면으로 간주 (±15° 정도)
    val yaw = headEulerAngleY
    val roll = headEulerAngleZ
    return (yaw in -15f..15f) && (roll in -15f..15f)
}

private fun com.google.mlkit.vision.face.Face.isSidePose(): Boolean {
    // 옆면: yaw가 크게 꺾여있고(예: ±70° 이상) 정면은 아닌 경우
    val yaw = headEulerAngleY
    return yaw > 60f || yaw < -60f
}

/* ========= 좌표 변환 유틸 ========= */

data class ScaleData(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val mirrorX: Boolean
)

fun calcFillCenter(
    imgW: Float, imgH: Float,
    viewW: Float, viewH: Float,
    mirrorX: Boolean
): ScaleData {
    val srcRatio = imgW / imgH
    val dstRatio = viewW / viewH
    val scale = if (srcRatio > dstRatio) viewH / imgH else viewW / imgW
    val scaledW = imgW * scale
    val scaledH = imgH * scale
    val offX = (viewW - scaledW) / 2f
    val offY = (viewH - scaledH) / 2f
    return ScaleData(scale, offX, offY, mirrorX)
}

fun RectF.mapToPreview(sd: ScaleData, viewW: Float) {
    left = left * sd.scale + sd.offsetX
    right = right * sd.scale + sd.offsetX
    top = top * sd.scale + sd.offsetY
    bottom = bottom * sd.scale + sd.offsetY

    if (sd.mirrorX) {
        val newLeft = viewW - right
        val newRight = viewW - left
        left = newLeft
        right = newRight
    }
}
