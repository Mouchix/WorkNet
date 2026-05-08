package com.example.worknet.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.worknet.ui.components.BottomBar
import com.example.worknet.ui.home.HomeScreen
import com.example.worknet.ui.favourites.FavouritesScreen
import com.example.worknet.ui.favourites.FavouritesViewModel
import com.example.worknet.ui.home.HomeViewModel
import com.example.worknet.ui.notifications.NotificationsScreen
import com.example.worknet.ui.place.PlaceDetailScreen
import com.example.worknet.ui.place.PlaceDetailViewModel
import com.example.worknet.ui.profile.ProfileScreen
import com.example.worknet.ui.profile.ProfileViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
            composable<NavigationRoute.Home> {
                val homeViewModel: HomeViewModel = koinViewModel()
                HomeScreen(navController, homeViewModel)
            }

            composable<NavigationRoute.Favourites> {
                val favViewModel: FavouritesViewModel = koinViewModel()
                FavouritesScreen(navController, favViewModel)
            }

            composable<NavigationRoute.Notifications> { NotificationsScreen(navController) }

            composable<NavigationRoute.Profile> {
                val profileViewModel: ProfileViewModel = koinViewModel()
                ProfileScreen(navController, profileViewModel)
            }

            composable<NavigationRoute.PlaceDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<NavigationRoute.PlaceDetail>()

                // Passiamo il placeId a Koin affinché possa iniettarlo nel ViewModel
                val placeDetailViewModel: PlaceDetailViewModel = koinViewModel { parametersOf(args.placeId) }

                PlaceDetailScreen(navController, placeDetailViewModel)
            }
        }
    }
}
