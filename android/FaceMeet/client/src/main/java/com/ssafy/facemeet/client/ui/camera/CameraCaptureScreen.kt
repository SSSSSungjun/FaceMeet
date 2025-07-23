@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.ssafy.facemeet.client.ui.camera

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ssafy.facemeet.client.camerax.fixRotation
import com.ssafy.facemeet.client.ml.FaceAnalyzer
import com.ssafy.facemeet.client.ml.FaceOvalSpec
import com.ssafy.facemeet.client.ml.FaceState
import com.ssafy.facemeet.client.ui.camera.component.CaptureChecklistBar
import com.ssafy.facemeet.client.ui.camera.component.FaceGuideOverlay
import com.ssafy.facemeet.client.ui.camera.component.FaceSideOverlay


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCaptureScreen(
    mode: CaptureMode,
    vm: CameraShotViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onCaptured: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    // 권한
    val cameraPerm = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPerm.status.isGranted) cameraPerm.launchPermissionRequest()
        else controller.bindToLifecycle(lifecycleOwner)
    }
    LaunchedEffect(cameraPerm.status.isGranted) {
        if (cameraPerm.status.isGranted) controller.bindToLifecycle(lifecycleOwner)
    }

    // 상태
    val spec = remember { FaceOvalSpec() }
    var faceState by remember { mutableStateOf(FaceState.OUTSIDE) }
    var countDown by remember { mutableStateOf<Int?>(null) }
    var analyzer: FaceAnalyzer? by remember { mutableStateOf(null) }
    DisposableEffect(controller, mode) {
        val a = FaceAnalyzer(
            context = context,
            controller = controller,
            previewViewState = previewViewRef,
            ovalSpec = spec,
            mode = mode,   // ui.camera.CaptureMode
            onHoldDone = {
                controller.takePicture(                       // ← 여기!
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        @Suppress("UnsafeOptInUsageError")
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bmp = image.toBitmap()
                                .fixRotation(
                                    rotationDegrees = image.imageInfo.rotationDegrees,
                                    mirror = controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                                )
                            image.close()

                            if (mode == CaptureMode.FRONT) vm.setFront(bmp) else vm.setSide(bmp)
                            onCaptured()
                        }

                        override fun onError(exc: ImageCaptureException) {
                            // 실패 처리만 해 주세요 (토스트/로그 등)
                            Log.e("CameraX", "capture failed", exc)
                        }

                    }
                )
            },

            onProgress = { sec -> countDown = if (sec in 1..3) sec else null },
            onStateChanged = { st -> faceState = st }
        )
        analyzer = a
        a.bind()
        onDispose { analyzer?.clear() }
    }



    Box(Modifier.fillMaxSize()) {

        CaptureChecklistBar(
            frontDone = vm.front.value != null,
            sideDone = vm.side.value != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = controller
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewViewRef.value = this
                }
            }
        )

        if (mode == CaptureMode.FRONT) {
            FaceGuideOverlay(
                countDown = countDown,
                state = faceState,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            )
        } else {
            FaceSideOverlay(
                countDown = countDown,
                guideText = "옆모습이 가이드에 들어오도록 맞춰주세요",
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            )
        }

        Button(
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(3f),
            onClick = onNavigateBack
        ) { Text("Back") }
    }
}
