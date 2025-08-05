package com.ssafy.facemeet.client.navigation.client

sealed class ClientRoutes(val route: String) {
    // 메인 앱 라우트들
    object MainMenu : ClientRoutes("main_menu")
    object Notification : ClientRoutes("notification") //얘는 얼굴 고치는
    object MatchingLoading : ClientRoutes("matching_loading")
    object Profile : ClientRoutes("profile")
    object PartnerProfile : ClientRoutes("partner_profile") {
        fun routeWithArgs(partnerId: Long, roomId: Long): String = "$route/$partnerId/$roomId"
    }

    //object MyPage : ClientRoutes("my_page")
    object Chat {
        const val route = "chat?roomId={roomId}&receiverId={receiverId}"

        fun createRoute(roomId: Long, receiverId: Long? = null): String {
            return if (receiverId != null)
                "chat?roomId=$roomId&receiverId=$receiverId"
            else
                "chat?roomId=$roomId"
        }
    }

}

