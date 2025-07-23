package com.ssafy.facemeet.core.constant

enum class AuthStatus {
    LOGGED_IN,      // 토큰 유효
    NEED_LOGIN,     // 토큰 없거나 갱신 실패
    NEED_SIGNUP     // 로그인은 됐지만 추가 정보 필요 (선택적)
}