package com.ssafy.facemeet.client.navigation.client

sealed class ClientRoutes(val route: String) {
    // 메인 앱 라우트들
    object MainMenu : ClientRoutes("main_menu")
    object MatchingLoading : ClientRoutes("matching_loading")
    object Profile : ClientRoutes("profile")

    object Chat : ClientRoutes("chat/{matchingId}") {
        fun createRoute(matchingId: String) = "chat/$matchingId"
    }
}

