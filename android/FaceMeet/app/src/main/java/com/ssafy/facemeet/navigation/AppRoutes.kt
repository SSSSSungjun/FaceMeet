package com.ssafy.facemeet.navigation

sealed class AppRoutes(val route: String) {
    object WebLogin : AppRoutes("webview/{provider}") {
        fun createRoute(provider: String) = "webview/$provider"
    }
}