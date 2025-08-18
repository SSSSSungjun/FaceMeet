package com.ssafy.facemeet.core.util.Animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

object NavigationAnimations {

    fun defaultEnterTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(300))
    }

    fun defaultExitTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(300))
    }

    fun getBottomNavEnterTransition(animationDirection: String): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val direction = if (animationDirection == "left") {
            AnimatedContentTransitionScope.SlideDirection.Left
        } else {
            AnimatedContentTransitionScope.SlideDirection.Right
        }

        fadeIn(animationSpec = tween(300)) + slideIntoContainer(
            towards = direction,
            animationSpec = tween(300)
        )
    }

    fun getBottomNavExitTransition(animationDirection: String): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val direction = if (animationDirection == "left") {
            AnimatedContentTransitionScope.SlideDirection.Left
        } else {
            AnimatedContentTransitionScope.SlideDirection.Right
        }

        fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
            towards = direction,
            animationSpec = tween(300)
        )
    }

}