package com.ssafy.facemeet.client.ui.chat

sealed class ChatNaviEvent {
    object ToBack : ChatNaviEvent()
}

sealed class ScrollEvent {
    object ToBottom : ScrollEvent()
    object WithKeyboard : ScrollEvent()
}

