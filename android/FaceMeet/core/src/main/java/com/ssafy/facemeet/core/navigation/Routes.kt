package com.ssafy.facemeet.core.navigation

sealed class Routes(val route: String) {

    //App 레벨 진입점
    object Login : Routes("login")

    //client 레벨 진입
    object ClientMain : Routes("client_main")
    object Setting : Routes("setting")
    object CameraCapture : Routes("cameraCapture")

    //Admin 레벨 진입
    object AdminMain : Routes("admin_main")
}
