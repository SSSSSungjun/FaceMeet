package com.ssafy.facemeet.client.ui.register

sealed class RegisterNaviEvent() {
    object ToCamera : RegisterNaviEvent()
    object ToMap : RegisterNaviEvent()
    object ToBack : RegisterNaviEvent()
}