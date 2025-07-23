@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.ssafy.facemeet.client.camerax

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * ImageProxy → Bitmap (회전 & 전면카메라 미러 옵션 지원)
 */
fun ImageProxy.toBitmap(
    rotationDegrees: Int = imageInfo.rotationDegrees,
    mirror: Boolean = false
): Bitmap {
    val nv21 = toNv21()
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)

    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val jpegBytes = out.toByteArray()

    var bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    if (rotationDegrees != 0 || mirror) {
        val m = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
            if (mirror) {
                postScale(-1f, 1f, bmp.width / 2f, bmp.height / 2f)
            }
        }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }
    return bmp
}

/**
 * ImageProxy planes -> NV21 byte array
 */
fun ImageProxy.toNv21(): ByteArray {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Y 그대로
    yBuffer.get(nv21, 0, ySize)

    // VU 순서로 NV21 만들기
    val chromaRowStride = planes[1].rowStride
    val chromaPixelStride = planes[1].pixelStride

    var offset = ySize
    // Interleave U and V data
    for (row in 0 until height / 2) {
        var col = 0
        while (col < width / 2) {
            val vuIndex = row * chromaRowStride + col * chromaPixelStride
            // V
            nv21[offset++] = vBuffer.get(vuIndex)
            // U
            nv21[offset++] = uBuffer.get(vuIndex)
            col++
        }
    }
    return nv21
}
