package com.ssafy.facemeet.client.ml

/**
 * Overlay와 Analyzer가 공유하는 타원 스펙 (비율)
 * Overlay는 고정 계산으로 그리고, Analyzer는 이 비율로 위치만 비교
 */
data class FaceOvalSpec(
    val widthRatio: Float = 0.70f,                 // 화면 짧은 변 기준 70%
    val heightRatio: Float = 0.70f / 0.75f,        // 3:4 비율 (W/H = 0.75)
    val centerXRatio: Float = 0.5f,
    val centerYRatio: Float = 0.55f
)