package com.ssafy.facemeet.core.util

import android.util.Log

object AppStateManager {
    private var _currentScreen: String? = null
    private var _currentRoomId: Long? = null

    fun setCurrentScreen(screenName: String, roomId: Long? = null) {
        _currentScreen = screenName
        _currentRoomId = roomId
        Log.d("AppState", "화면 변경: $screenName, 방ID: $roomId")
    }

    fun getCurrentScreen(): String? = _currentScreen

    fun isInChatRoom(roomId: Long): Boolean {
        return _currentScreen == "ChattingScreen" && _currentRoomId == roomId
    }

    fun getCurrentRoomId(): Long? = _currentRoomId

    fun clearCurrentScreen() {
        Log.d("AppState", "화면 상태 초기화")
        _currentScreen = null
        _currentRoomId = null
    }
}
