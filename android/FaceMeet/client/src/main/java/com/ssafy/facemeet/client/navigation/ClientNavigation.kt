package com.ssafy.facemeet.client.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.client.mainmenu.MainMenuScreen
import com.ssafy.facemeet.core.navigation.Routes


fun NavGraphBuilder.clientNavigation(parentNavController: NavHostController) { //클라이언트 모듈로 왔을 때 다시 NavHost 정의
    composable(Routes.ClientMain.route) {
        ClientNavHost(
            onNavigateBack = {
                parentNavController.popBackStack() // 필요시 App 레벨로 돌아가기
            }
        )
    }
}