package com.ssafy.facemeet.client.ui.camera

import android.Manifest
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ssafy.facemeet.client.camerax.takePhotoAndSave
import com.ssafy.facemeet.client.ml.FaceAnalyzer
import com.ssafy.facemeet.client.ml.FaceOvalSpec
import com.ssafy.facemeet.client.ml.FaceState
import com.ssafy.facemeet.client.ui.camera.component.FaceGuideOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCaptureScreen(
    onCaptured: (android.net.Uri) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    // PreviewView 참조
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    // 권한
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        } else controller.bindToLifecycle(lifecycleOwner)
    }
    LaunchedEffect(cameraPermission.status.isGranted) {
        if (cameraPermission.status.isGranted) controller.bindToLifecycle(lifecycleOwner)
    }

    val spec = remember { FaceOvalSpec() }
    var analyzer: FaceAnalyzer? by remember { mutableStateOf(null) }
    var countDown by remember { mutableStateOf<Int?>(null) }
    var faceState by remember { mutableStateOf(FaceState.OUTSIDE) }

    DisposableEffect(controller) {
        val a = FaceAnalyzer(
            context = context,
            controller = controller,
            previewViewState = previewViewRef,
            ovalSpec = spec,
            onHoldDone = {
                takePhotoAndSave(
                    controller = controller,
                    context = context,
                    onSaved = { uri ->
                        Toast.makeText(context, "Saved: $uri", Toast.LENGTH_SHORT).show()
                        onCaptured(uri)
                    }
                )
            },
            onProgress = { sec -> countDown = if (sec in 1..3) sec else null },
            onStateChanged = { state -> faceState = state }
        )
        analyzer = a
        a.bind()

        onDispose { analyzer?.clear() }
    }

    Box(Modifier.fillMaxSize()) {
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

        FaceGuideOverlay(
            countDown = countDown,
            state = faceState,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        )

        Button(
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(2f),
            onClick = onNavigateBack
        ) { Text("Back") }
    }
}
