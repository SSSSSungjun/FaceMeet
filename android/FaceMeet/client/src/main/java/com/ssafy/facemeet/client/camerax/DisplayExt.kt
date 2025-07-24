package com.ssafy.facemeet.client.camerax

import android.view.Display
import android.view.Surface

fun Display.rotationDegreesCompat(): Int = when (rotation) {
    Surface.ROTATION_0 -> 270
    Surface.ROTATION_90 -> 180
    Surface.ROTATION_180 -> 90
    Surface.ROTATION_270 -> 0
    else -> 0
}
