// File: client/src/main/java/com/ssafy/facemeet/client/ui/camera/CameraCaptureScreen.kt
package com.ssafy.facemeet.client.ui.camera

import FaceSideOverlay
import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ssafy.facemeet.client.camerax.fixRotation
import com.ssafy.facemeet.client.ml.FaceAnalyzer
import com.ssafy.facemeet.client.ml.FaceOvalSpec
import com.ssafy.facemeet.client.ml.FaceState
import com.ssafy.facemeet.client.ui.camera.component.FaceGuideOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCaptureScreen(
    mode: CaptureMode,
    vm: CameraShotViewModel = viewModel(),
    onCaptured: () -> Unit = {},
    onNavigateToBack: () -> Unit = {}

) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 항상 전면 카메라 사용
    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            this.cameraSelector = cameraSelector
        }
    }
    val previewRef = remember { mutableStateOf<PreviewView?>(null) }

    // 권한 처리
    val perm = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(perm.status.isGranted) {
        if (perm.status.isGranted) controller.bindToLifecycle(lifecycleOwner)
        else perm.launchPermissionRequest()
    }

    // Face guide state
    val spec = remember { FaceOvalSpec() }
    var faceState by remember { mutableStateOf(FaceState.OUTSIDE) }
    var countDown by remember { mutableStateOf<Int?>(null) }
    var analyzer by remember { mutableStateOf<FaceAnalyzer?>(null) }
    val shutter = remember { ShutterPlayer(context) }
    val flashAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    DisposableEffect(controller, mode) {
        val fa = FaceAnalyzer(
            context = context,
            controller = controller,
            previewViewState = previewRef,
            ovalSpec = spec,
            mode = mode,
            onHoldDone = {
                // onHoldDone 내부에서 (셔터 사운드/촬영 호출과 함께)
                scope.launch {
                    flashAlpha.stop()

                    // 1) 번쩍: 빠르게 1.0까지
                    flashAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 60, easing = LinearEasing)
                    )

                    // 2) 꼭대기 유지(여기 값을 늘리면 더 오래 유지됨)
                    withFrameNanos { }        // 1프레임 보장(가끔 1.0 프레임 스킵 방지)
                    delay(150)                // ← 완전 흰색 유지 시간(ms) 예: 150~250

                    // 3) 서서히 사라짐
                    flashAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
                    )
                }
                shutter.play(0.10f) // ← 아주 작게 “찰칵”
                controller.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        @Suppress("UnsafeOptInUsageError")
                        override fun onCaptureSuccess(imageProxy: androidx.camera.core.ImageProxy) {
                            val raw = imageProxy.toBitmap()
                            imageProxy.close()
                            val fixed = raw.fixRotation(
                                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                                mirror = (mode == CaptureMode.FRONT)
                            )

                            if (mode == CaptureMode.FRONT) vm.setFront(fixed) else vm.setSide(fixed)
                            onCaptured()
                        }

                        override fun onError(exc: ImageCaptureException) {
                        }
                    }
                )

            },
            onProgress = { sec -> countDown = if (sec in 0..3) sec else null },
            onStateChanged = { faceState = it },
        )
        analyzer = fa
        fa.bind()
        onDispose {
            analyzer?.clear()
            shutter.release() // 해제
        }
    }

    Box(Modifier.fillMaxSize()) {
        // 프리뷰
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = controller
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewRef.value = this
                }
            }
        )
        // overlay 선택
        if (mode == CaptureMode.FRONT) {
            FaceGuideOverlay(countDown, faceState)
        } else {
            FaceSideOverlay(countDown, "옆모습을 가이드에 맞춰주세요")
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = flashAlpha.value))
                .zIndex(3f) // 모든 UI 위로
        )

    }
}
