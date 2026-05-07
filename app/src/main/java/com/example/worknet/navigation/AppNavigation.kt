package com.example.worknet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.worknet.ui.home.HomeScreen
import com.example.worknet.ui.favourites.FavouritesScreen
import com.example.worknet.ui.notifications.NotificationsScreen
import com.example.worknet.ui.profile.ProfileScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Home
    ) {
        composable<NavigationRoute.Home> { HomeScreen(navController) }
        composable<NavigationRoute.Home> { FavouritesScreen(navController) }
        composable<NavigationRoute.Home> { NotificationsScreen(navController) }
        composable<NavigationRoute.Home> { ProfileScreen(navController) }
    }
}
