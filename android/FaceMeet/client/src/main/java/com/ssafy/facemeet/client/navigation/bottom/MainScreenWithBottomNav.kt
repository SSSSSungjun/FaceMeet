package com.ssafy.facemeet.client.navigation.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.ui.chatlist.ChattingListScreen
import com.ssafy.facemeet.client.ui.mainmenu.MainMenuScreen
import com.ssafy.facemeet.client.ui.matching.MatchingScreen
import com.ssafy.facemeet.client.ui.mypage.MyPageScreen

@Composable
fun MainScreenWithBottomNav(
    mainNavController: NavHostController,
    initialTab: String = BottomNavRoutes.Home.route
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F3ED)),
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
                    onMyPage = {
                        bottomNavController.navigate(BottomNavRoutes.MyPage.route)
                    },
                    onNotification = {
                        mainNavController.navigate(ClientRoutes.Notification.route)
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
                    onItemClick = {
                        mainNavController.navigate(ClientRoutes.Chat.route)
                    } // chatItems의 Id값 정도 인자값으로 넘기면 됨
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
            Triple(BottomNavRoutes.Home.route, Icons.Default.Home, "홈"),
            Triple(BottomNavRoutes.Matching.route, Icons.Default.Favorite, "매칭"),
            Triple(BottomNavRoutes.ChattingList.route, Icons.AutoMirrored.Filled.List, "채팅"),
            Triple(BottomNavRoutes.MyPage.route, Icons.Default.Person, "마이페이지")
        )
    }

    NavigationBar {
        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}