package com.ssafy.facemeet.core.util

import android.util.Log

object AppStateManager {
    private var _currentScreen: String? = null
    private var _currentRoomId: Long? = null

    fun setCurrentScreen(screenName: String, roomId: Long? = null) {
        _currentScreen = screenName
        _currentRoomId = roomId
        Log.d("AppState", "현재 화면: $screenName, 방ID: $roomId")
    }

    fun getCurrentScreen(): String? = _currentScreen // 추가

    fun isInChatRoom(roomId: Long): Boolean {
        return _currentScreen == "ChattingScreen" && _currentRoomId == roomId
    }

    fun getCurrentRoomId(): Long? = _currentRoomId
}