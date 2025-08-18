package com.ssafy.facemeet.client.ui.map

sealed class MapNaviEvent() {
    object ToBack : MapNaviEvent()
    object ToAccept : MapNaviEvent()
}
