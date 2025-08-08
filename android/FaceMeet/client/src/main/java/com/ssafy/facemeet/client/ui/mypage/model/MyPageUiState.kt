package com.ssafy.facemeet.client.ui.mypage.model

import com.ssafy.facemeet.core.domain.model.UserInfo

data class MyPageUiState(
    val isLoading: Boolean = false,
    val userProfile: UserInfo? = null,
    val marketingAlarmEnabled: Boolean = false, // 로컬 설정
    val isWithdrawBtnClicked: Boolean = false,
    val error: String? = null
)
