package com.ssafy.facemeet.core.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Main : Routes("main")
    object Admin : Routes("admin")
    object Setting : Routes("setting")
}
