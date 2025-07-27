package com.ssafy.facemeet.client.navigation.client

sealed class ClientRoutes(val route: String) {
    // 메인 앱 라우트들
    object MainMenu : ClientRoutes("main_menu")
    object FaceResult : ClientRoutes("face_result")
    object MatchingLoading : ClientRoutes("matching_loading")

    object Profile : ClientRoutes("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object Chat : ClientRoutes("chat/{matchingId}") {
        fun createRoute(matchingId: String) = "chat/$matchingId"
    }
}

