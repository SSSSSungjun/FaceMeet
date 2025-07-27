package com.ssafy.facemeet.client.navigation

sealed class ClientRoutes(val route: String) {
    object MainMenu : ClientRoutes("main_menu")
    object Chatting : ClientRoutes("chatting")
    object Setting : ClientRoutes("setting")

    // 새로 추가
    object FrontCamera : ClientRoutes("front_camera")
    object FrontPreview : ClientRoutes("front_preview")
    object SideCamera : ClientRoutes("side_camera")
    object SidePreview : ClientRoutes("side_preview")
    object FaceTestLoading : ClientRoutes("face_test_loading")
    object FaceTestResult : ClientRoutes("face_test_result")
}
