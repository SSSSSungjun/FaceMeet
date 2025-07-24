package com.ssafy.facemeet.client.camerax

import android.graphics.Bitmap
import android.graphics.Matrix

/** 각도만큼 회전 */
fun Bitmap.rotate(deg: Float): Bitmap {
    if (deg % 360f == 0f) return this
    val m = Matrix().apply { postRotate(deg) }
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}

/** 좌우 반전(전면 카메라 셀피용) */
fun Bitmap.mirrorX(): Bitmap {
    val m = Matrix().apply { postScale(-1f, 1f, width / 2f, height / 2f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}
