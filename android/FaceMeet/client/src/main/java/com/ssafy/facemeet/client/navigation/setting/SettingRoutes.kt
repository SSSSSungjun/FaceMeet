package com.ssafy.facemeet.client.navigation.setting

sealed class SettingRoutes(val route: String) {

    object Register : SettingRoutes("register")
    object CameraStart : SettingRoutes("camera_start")
    object FrontCamera : SettingRoutes("front_camera")
    object FrontPreview : SettingRoutes("front_preview")
    object SideCamera : SettingRoutes("side_camera")
    object SidePreview : SettingRoutes("side_preview")
    object FaceTestLoading : SettingRoutes("face_test_loading")
    object Map : SettingRoutes("map")
}