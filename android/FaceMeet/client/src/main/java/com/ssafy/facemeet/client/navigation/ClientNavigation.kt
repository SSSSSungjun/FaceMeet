package com.ssafy.facemeet.client.navigation

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.facemeet.core.navigation.Routes

private const val TAG = "ClientNavigation"
fun NavGraphBuilder.clientNavigation(parentNavController: NavHostController) { //클라이언트 모듈로 왔을 때 다시 NavHost 정의
    composable(Routes.Setting.route) {
        Log.d(TAG, "clientNavigation: 진입 성공")
        ClientNavHost(
            onNavigateBack = {
                parentNavController.popBackStack() // 필요시 App 레벨로 돌아가기
            }
        )
    } // 이것도 임시 ㅋㅋ
}