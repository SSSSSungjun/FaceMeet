package com.ssafy.facemeet.client.navigation.bottom

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.ui.chatlist.ChattingListScreen
import com.ssafy.facemeet.client.ui.mainmenu.MainMenuScreen
import com.ssafy.facemeet.client.ui.matching.MatchingScreen
import com.ssafy.facemeet.client.ui.mypage.MyPageScreen
import com.ssafy.facemeet.core.util.constant.CommonColor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreenWithBottomNav(
    mainNavController: NavHostController,
    //bottomNavController: NavHostController,
    initialTab: String = BottomNavRoutes.Home.route
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color(0xFFF4F3ED),
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = initialTab,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavRoutes.Home.route) {
                MainMenuScreen(
                    onProfile = {
                        mainNavController.navigate(ClientRoutes.Profile.route)
                    },
                    onNotification = {
                        mainNavController.navigate(ClientRoutes.Notification.route)
                    },
                    onMatch = {
                        bottomNavController.navigate(BottomNavRoutes.Matching.route)
                    }
                )
            }

            composable(BottomNavRoutes.Matching.route) {
                MatchingScreen(
                    onStartMatching = {
                        mainNavController.navigate(ClientRoutes.MatchingLoading.route)
                    }
                )
            }

            composable(BottomNavRoutes.ChattingList.route) {
                ChattingListScreen(
                    onItemClick = { item ->
                        val roomId = item.chatRoomId
                        val matcingUserId = item.userId
                        mainNavController.navigate(ClientRoutes.Chat.createRoute(roomId))
                    }
                )
            }

            composable(BottomNavRoutes.MyPage.route) {
                MyPageScreen(
                    onLogout = {
                        mainNavController.navigate("start") {
                            popUpTo(0) { inclusive = false }
                        }
                    },
                    onWithdraw = {
                        mainNavController.navigate("start") {
                            popUpTo(0) { inclusive = false }
                        }
                    },
                    onModify = {
                        mainNavController.navigate(SettingRoutes.Register.route)
                    }
                )
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = remember {
        listOf(
            BottomNavItem(
                BottomNavRoutes.Home.route,
                R.drawable.ic_bottom_navi_home,
                R.drawable.ic_bottom_navi_home_fill,
                "홈"
            ),
            BottomNavItem(
                BottomNavRoutes.Matching.route,
                R.drawable.ic_bottom_navi_match,
                R.drawable.ic_bottom_navi_match_fill,
                "매칭"
            ),
            BottomNavItem(
                BottomNavRoutes.ChattingList.route,
                R.drawable.ic_bottom_navi_chat,
                R.drawable.ic_bottom_navi_chat_fill,
                "채팅"
            ),
            BottomNavItem(
                BottomNavRoutes.MyPage.route,
                R.drawable.ic_bottom_navi_setting,
                R.drawable.ic_bottom_navi_setting_fill,
                "마이페이지"
            )
        )
    }

    NavigationBar(
        containerColor = CommonColor.Beige100
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = if (selected) item.selectedIcon else item.icon),
                        contentDescription = item.label, tint = CommonColor.Brown500
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CommonColor.Beige200
                )
            )
        }
    }

}

data class BottomNavItem(
    val route: String,
    val icon: Int,
    val selectedIcon: Int,
    val label: String
)