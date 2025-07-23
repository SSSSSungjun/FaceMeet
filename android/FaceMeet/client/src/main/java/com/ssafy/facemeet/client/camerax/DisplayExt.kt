package com.ssafy.facemeet.client.camerax

import android.view.Display
import android.view.Surface

fun Display.rotationDegreesCompat(): Int = when (rotation) {
    Surface.ROTATION_0 -> 0
    Surface.ROTATION_90 -> 90
    Surface.ROTATION_180 -> 180
    Surface.ROTATION_270 -> 270
    else -> 0
}
