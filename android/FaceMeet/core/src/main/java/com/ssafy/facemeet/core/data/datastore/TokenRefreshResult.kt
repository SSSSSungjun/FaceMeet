package com.ssafy.facemeet.core.data.datastore

sealed class TokenRefreshResult {
    object Success : TokenRefreshResult()      // 토큰 갱신 성공
    object Failed : TokenRefreshResult()       // 토큰 갱신 실패 (재로그인 필요)
}