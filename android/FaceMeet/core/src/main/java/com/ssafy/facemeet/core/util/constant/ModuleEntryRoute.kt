package com.ssafy.facemeet.core.util.constant

sealed class ModuleEntryRoute(val route: String) {
    object Register : ModuleEntryRoute("register")
    object ClientMainMenu : ModuleEntryRoute("main_menu")
    object Start : ModuleEntryRoute("start")
}