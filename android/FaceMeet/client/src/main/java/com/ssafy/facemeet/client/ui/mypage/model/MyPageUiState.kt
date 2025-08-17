package com.ssafy.facemeet.client.ui.mypage.model

import com.ssafy.facemeet.core.domain.model.UserInfo

data class MyPageUiState(
    val isLoading: Boolean = false,
    val userInfo: UserInfo = UserInfo(),
    val isWithdrawBtnClicked: Boolean = false,
    val isTogglingMarketing: Boolean = false,
    val error: String? = null
)
