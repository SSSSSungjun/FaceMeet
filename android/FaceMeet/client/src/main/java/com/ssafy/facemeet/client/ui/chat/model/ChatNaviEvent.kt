package com.ssafy.facemeet.client.ui.chat.model

sealed class ChatNaviEvent {
    object ToBack : ChatNaviEvent()
    object ToProfile : ChatNaviEvent()
}

sealed class ScrollEvent {
    object ToBottom : ScrollEvent()
    object WithKeyboard : ScrollEvent()
}

