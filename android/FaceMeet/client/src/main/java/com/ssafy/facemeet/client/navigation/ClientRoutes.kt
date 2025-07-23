package com.ssafy.facemeet.client.navigation

sealed class ClientRoutes(val route: String) {
    object MainMenu : ClientRoutes("main_menu")
    object Chatting : ClientRoutes("chatting")
    //클라이언트 내부에서의
}