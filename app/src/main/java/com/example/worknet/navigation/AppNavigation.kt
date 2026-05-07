package com.example.worknet.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.worknet.ui.components.BottomBar
import com.example.worknet.ui.home.HomeScreen
import com.example.worknet.ui.favourites.FavouritesScreen
import com.example.worknet.ui.notifications.NotificationsScreen
import com.example.worknet.ui.profile.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoute.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<NavigationRoute.Home> { HomeScreen(navController) }
            composable<NavigationRoute.Favourites> { FavouritesScreen(navController) }
            composable<NavigationRoute.Notifications> { NotificationsScreen(navController) }
            composable<NavigationRoute.Profile> { ProfileScreen(navController) }
        }
    }
}
