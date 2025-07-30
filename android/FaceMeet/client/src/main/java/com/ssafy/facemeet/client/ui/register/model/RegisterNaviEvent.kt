package com.ssafy.facemeet.client.ui.register.model

sealed class RegisterNaviEvent() {
    object ToCamera : RegisterNaviEvent()
    object ToMap : RegisterNaviEvent()
    object ToBack : RegisterNaviEvent()
}