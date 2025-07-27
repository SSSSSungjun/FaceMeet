package com.ssafy.facemeet.ui.web

enum class SocialProvider {
    KAKAO,
    NAVER,
    NONE;

    companion object {
        fun from(value: String?): SocialProvider {
            return when (value?.uppercase()) {
                "KAKAO" -> KAKAO
                "NAVER" -> NAVER
                else -> NONE // 기본값
            }
        }
    }
}