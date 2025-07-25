@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.ssafy.facemeet.client.camerax

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat

/**
 * CameraX 컨트롤러로 바로 캡처해서 Bitmap 콜백으로 넘김(임시 저장 X)
 */
fun takePhotoTemp(
    controller: LifecycleCameraController,
    context: Context,
    onBitmap: (Bitmap) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            @Suppress("UnsafeOptInUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                val bmp = image.toBitmap()              // ← 너가 만든 ext 함수
                image.close()
                onBitmap(bmp)
            }

            override fun onError(exception: ImageCaptureException) {
                // 필요하면 로그 남기고 image 닫기
            }
        }
    )
}
