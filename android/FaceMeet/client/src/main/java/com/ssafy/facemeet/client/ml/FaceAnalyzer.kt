@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.ssafy.facemeet.client.ml

import android.content.Context
import android.graphics.RectF
import android.util.Log
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
import kotlin.math.floor
import kotlin.math.pow

/* ===========================
 *  CONFIG / CONSTANTS
 * =========================== */

private const val TAG = "FaceAnalyzer"

// 홀드 시간 (ms)
private const val HOLD_MS = 3_600L

// 얼굴 높이(화면 대비) 허용 범위
private const val MIN_FACE_H_RATIO = 0.18f
private const val MAX_FACE_H_RATIO = 0.60f

// 연속 OK 프레임 수
private const val OK_STREAK_NEED = 3

// 타원 여유 %
private const val OVAL_PADDING = 0.52f

// 정면 포즈 허용 (yaw, roll 기준)
private const val FRONT_YAW_MAX = 12f
private const val FRONT_ROLL_MAX = 15f

// 옆면 포즈 허용 범위 (절대값)
private const val SIDE_MIN_YAW = 20f   // 최소 30도 정도는 돌아가야
private const val SIDE_MAX_YAW = 55f   // 55도 넘게 돌아가면 "너무 옆"으로 간주

/* ===========================
 *  MAIN CLASS
 * =========================== */

class FaceAnalyzer(
    private val context: Context,
    private val controller: LifecycleCameraController,
    private val previewViewState: State<PreviewView?>,
    private val ovalSpec: FaceOvalSpec,
    private val mode: CaptureMode,
    private val onHoldDone: () -> Unit,
    private val onProgress: (Int) -> Unit,
    private val onStateChanged: (FaceState) -> Unit,
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

                    if (faces.isEmpty()) {
                        Log.d(TAG, "No face detected")
                    }

                    faces.forEach { face ->
                        val yaw = face.headEulerAngleY
                        val roll = face.headEulerAngleZ

                        val posePass = when (mode) {
                            CaptureMode.FRONT -> face.isFrontPose(yaw, roll)
                            CaptureMode.SIDE -> face.isSidePose(yaw)
                        }
                        if (!posePass) {
                            Log.v(TAG, "Pose fail -> mode=$mode, yaw=$yaw, roll=$roll")
                            return@forEach
                        }

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

                        Log.v(
                            TAG,
                            "mode=$mode, yaw=$yaw, roll=$roll, hRatio=$hRatio, state=$state"
                        )

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
                            val secLeft = floor(remainMs / 1000.0).toInt().coerceAtLeast(0)

                            if (secLeft != lastReported && !fired) {
                                lastReported = secLeft
                                if (secLeft in 0..3) onProgress(secLeft)
                            }
                            if (!fired && secLeft == 0) {
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
                .addOnFailureListener { e ->
                    Log.e(TAG, "MLKit process error: ${e.message}", e)
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    fun clear() {
        controller.clearImageAnalysisAnalyzer()
        detector.close()
    }
}

/* ===========================
 *  Pose Helpers
 * =========================== */

private fun com.google.mlkit.vision.face.Face.isFrontPose(yaw: Float, roll: Float): Boolean {
    return (yaw in -FRONT_YAW_MAX..FRONT_YAW_MAX) && (roll in -FRONT_ROLL_MAX..FRONT_ROLL_MAX)
}

private fun com.google.mlkit.vision.face.Face.isSidePose(yaw: Float): Boolean {
    val absYaw = kotlin.math.abs(yaw)
    return absYaw in SIDE_MIN_YAW..SIDE_MAX_YAW
}

/* ===========================
 *  Transform utils
 * =========================== */

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
