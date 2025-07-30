package com.ssafy.facemeet.client.ui.mypage.model

sealed class MyPageNaviEvent() {
    object ToModify : MyPageNaviEvent()
    object ToLogout : MyPageNaviEvent()
    object ToWithdraw : MyPageNaviEvent()
}