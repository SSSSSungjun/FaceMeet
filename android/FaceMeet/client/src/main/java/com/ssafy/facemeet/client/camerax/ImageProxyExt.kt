package com.ssafy.facemeet.client.camerax

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy

/**
 * ImageProxy → ARGB_8888 Bitmap (YUV & JPEG 둘 다 처리)
 */
fun ImageProxy.toBitmap(): Bitmap {
    return when (format) {
        ImageFormat.JPEG -> {
            // JPEG면 그냥 ByteArray로 디코딩
            val buf = planes[0].buffer
            val bytes = ByteArray(buf.remaining())
            buf.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        else -> { // YUV_420_888
            val yuv = toNv21()
            val yuvImage = YuvImage(yuv, ImageFormat.NV21, width, height, null)
            val out = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val bytes = out.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}

/** YUV_420_888 → NV21 */
private fun ImageProxy.toNv21(): ByteArray {
    val ySize = planes[0].buffer.remaining()
    val uSize = planes[1].buffer.remaining()
    val vSize = planes[2].buffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    planes[0].buffer.get(nv21, 0, ySize)
    // VU 순서 맞추기
    val uBytes = ByteArray(uSize)
    val vBytes = ByteArray(vSize)
    planes[1].buffer.get(uBytes)
    planes[2].buffer.get(vBytes)
    // NV21 = Y + V + U
    System.arraycopy(vBytes, 0, nv21, ySize, vSize)
    System.arraycopy(uBytes, 0, nv21, ySize + vSize, uSize)

    return nv21
}

/**
 * 회전 + 미러 적용
 */
fun Bitmap.fixRotation(rotationDegrees: Int, mirror: Boolean): Bitmap {
    if (rotationDegrees == 0 && !mirror) return this

    val m = Matrix().apply {
        if (rotationDegrees != 0) postRotate(rotationDegrees.toFloat())
        if (mirror) postScale(-1f, 1f)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}
