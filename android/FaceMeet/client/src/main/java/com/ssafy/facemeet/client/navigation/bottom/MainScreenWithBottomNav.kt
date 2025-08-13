package com.ssafy.facemeet.client.navigation.bottom

import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.navigation.CurrentBottomNavState
import com.ssafy.facemeet.client.navigation.client.ClientRoutes
import com.ssafy.facemeet.client.navigation.setting.RegisterMode
import com.ssafy.facemeet.client.navigation.setting.SettingRoutes
import com.ssafy.facemeet.client.ui.chatlist.ChattingListScreen
import com.ssafy.facemeet.client.ui.mainmenu.MainMenuScreen
import com.ssafy.facemeet.client.ui.matching.MatchingScreen
import com.ssafy.facemeet.client.ui.mypage.MyPageScreen
import com.ssafy.facemeet.core.util.Animation.NavigationAnimations
import com.ssafy.facemeet.core.util.constant.CommonColor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreenWithBottomNav(
    mainNavController: NavHostController,
    //bottomNavController: NavHostController,
    initialTab: String = CurrentBottomNavState.currentBottomTab
) {
    val bottomNavController = rememberNavController()
    var animationDirection by rememberSaveable { mutableStateOf("left") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color(0xFFF4F3ED),
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                onDirectionChange = { direction ->
                    animationDirection = direction
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = initialTab,
            modifier = Modifier.padding(paddingValues),
            enterTransition = NavigationAnimations.getBottomNavEnterTransition(animationDirection),
            exitTransition = NavigationAnimations.getBottomNavExitTransition(animationDirection)
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
                        CurrentBottomNavState.currentBottomTab =
                            BottomNavRoutes.Matching.route // (선택)
                        bottomNavController.navigate(BottomNavRoutes.Matching.route) {
                            popUpTo(bottomNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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
                    onMoveAfterLogout = {
                        mainNavController.navigate("start") {
                            popUpTo(0) { inclusive = false }
                        }
                    },
                    onMoveAfterWithdraw = {
                        mainNavController.navigate("start") {
                            popUpTo(0) { inclusive = false }
                        }
                    },
                    onModify = {
                        mainNavController.navigate("${SettingRoutes.Register.route}?mode=${RegisterMode.EDIT.name}")
                    }, onOpenBlocked = {
                        mainNavController.navigate(ClientRoutes.BlockList.route)
                    }
                )
            }
        }
    }
}


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    onDirectionChange: (String) -> Unit
) {
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

    val currentIndex = items.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    NavigationBar(
        containerColor = CommonColor.Beige100
    ) {
        items.forEachIndexed { index, item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        // 방향 계산 후 상태 저장
                        val direction = if (index > currentIndex) "left" else "right"
                        Log.d("direction ", "BottomNavigationBar: ${direction}")
                        onDirectionChange(direction)
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
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
                label = {
                    Text(
                        item.label,
                        fontSize = 13.sp,
                        color = CommonColor.Brown500,
                        fontWeight = FontWeight.SemiBold
                    )
                },
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