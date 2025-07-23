package com.ssafy.facemeet.client.ml

import com.google.mlkit.vision.face.Face
import kotlin.math.abs

fun Face.isFront(threshold: Float = 15f): Boolean {
    return abs(headEulerAngleY) < threshold && abs(headEulerAngleZ) < threshold
}