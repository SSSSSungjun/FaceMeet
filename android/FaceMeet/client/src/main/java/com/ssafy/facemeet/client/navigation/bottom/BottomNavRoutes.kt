package com.ssafy.facemeet.client.navigation.bottom

sealed class BottomNavRoutes(val route: String) {
    object Home : BottomNavRoutes("home")
    object Matching : BottomNavRoutes("matching") // 매칭 시작 화면
    object ChattingList : BottomNavRoutes("chatting_list") // 채팅방 목록
    object MyPage : BottomNavRoutes("my_page")
}