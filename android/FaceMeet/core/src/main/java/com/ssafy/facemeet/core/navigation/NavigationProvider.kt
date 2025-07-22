package com.ssafy.facemeet.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController


interface NavigationProvider {
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    )
}
