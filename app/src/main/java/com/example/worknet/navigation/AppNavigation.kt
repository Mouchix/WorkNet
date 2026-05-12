package com.example.worknet.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.worknet.data.repository.UserRepository
import com.example.worknet.ui.components.bottomBar.BottomBar
import com.example.worknet.ui.components.bottomBar.BottomBarViewModel
import com.example.worknet.ui.home.HomeScreen
import com.example.worknet.ui.favourites.FavouritesScreen
import com.example.worknet.ui.favourites.FavouritesViewModel
import com.example.worknet.ui.home.HomeViewModel
import com.example.worknet.ui.notifications.NotificationsScreen
import com.example.worknet.ui.notifications.NotificationsViewModel
import com.example.worknet.ui.place.PlaceDetailScreen
import com.example.worknet.ui.place.PlaceDetailViewModel
import com.example.worknet.ui.profile.addplace.AddPlaceScreen
import com.example.worknet.ui.profile.addplace.AddPlaceViewModel
import com.example.worknet.ui.profile.ProfileScreen
import com.example.worknet.ui.profile.ProfileViewModel
import com.example.worknet.ui.profile.myPlaces.MyPlacesScreen
import com.example.worknet.ui.profile.myPlaces.MyPlacesViewModel
import com.example.worknet.ui.user.UserScreen
import com.example.worknet.ui.user.UserViewModel
import com.example.worknet.ui.profile.editProfile.EditProfileScreen
import com.example.worknet.ui.profile.editProfile.EditProfileViewModel
import com.example.worknet.ui.welcome.WelcomeScreen
import com.example.worknet.ui.welcome.WelcomeViewModel
import com.example.worknet.ui.welcome.logIn.LoginScreen
import com.example.worknet.ui.welcome.logIn.LoginViewModel
import com.example.worknet.ui.welcome.signIn.CreateProfileScreen
import com.example.worknet.ui.welcome.signIn.CreateProfileViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userRepository: UserRepository = koinInject()
    val bottomBarViewModel: BottomBarViewModel = koinViewModel()

    val currentUser = userRepository.getCurrentUserId()
    val startDestination = if (currentUser != null) {
        NavigationRoute.Home
    } else {
        NavigationRoute.Welcome
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = when {
        currentRoute == null -> false
        currentRoute.contains("Home") -> true
        currentRoute.contains("Favourites") -> true
        currentRoute.contains("Notifications") -> true
        currentRoute.contains("EditProfile") -> false
        currentRoute.contains("CreateProfile") -> false
        currentRoute.contains("Profile") -> true

        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController, bottomBarViewModel)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
        ) {
            composable<NavigationRoute.Home> {
                val homeViewModel: HomeViewModel = koinViewModel()
                HomeScreen(
                    navController,
                    homeViewModel,
                    innerPadding = innerPadding
                )
            }

            composable<NavigationRoute.Favourites> {
                val favViewModel: FavouritesViewModel = koinViewModel()
                FavouritesScreen(
                    navController,
                    favViewModel,
                    innerPadding = innerPadding
                )
            }

            composable<NavigationRoute.Notifications> {
                val notificationsViewModel: NotificationsViewModel = koinViewModel()
                NotificationsScreen(
                    navController = navController, 
                    viewModel = notificationsViewModel,
                    innerPadding = innerPadding
                ) 
            }

            composable<NavigationRoute.Profile> {
                val profileViewModel: ProfileViewModel = koinViewModel()
                ProfileScreen(navController, profileViewModel, Modifier.padding(innerPadding))
            }

            composable<NavigationRoute.PlaceDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<NavigationRoute.PlaceDetail>()

                // Passiamo il placeId a Koin affinché possa iniettarlo nel ViewModel
                val placeDetailViewModel: PlaceDetailViewModel = koinViewModel { parametersOf(args.placeId) }

                // Qui NON passiamo l'innerPadding (o lo gestiamo internamente)
                // così l'immagine può andare sotto la barra di stato
                PlaceDetailScreen(navController, placeDetailViewModel, Modifier.padding(innerPadding))
            }

            composable<NavigationRoute.AddPlace> {
                val addPlaceViewModel: AddPlaceViewModel = koinViewModel()
                AddPlaceScreen(navController, null,addPlaceViewModel)
            }

            composable<NavigationRoute.EditPlace> { backStackEntry ->
                val args = backStackEntry.toRoute<NavigationRoute.EditPlace>()
                val viewModel: AddPlaceViewModel = koinViewModel()
                AddPlaceScreen(
                    navController = navController,
                    placeId = args.placeId,
                    viewModel = viewModel
                )
            }


            composable<NavigationRoute.User> { backStackEntry ->
                val args = backStackEntry.toRoute<NavigationRoute.User>()
                val userViewModel: UserViewModel = koinViewModel { parametersOf(args.userId) }
                UserScreen(navController, userViewModel, Modifier.padding(innerPadding))
            }

            composable<NavigationRoute.MyPlaces> {
                val vm: MyPlacesViewModel = koinViewModel()
                MyPlacesScreen(navController, vm)
            }

            composable<NavigationRoute.EditProfile> { backStackEntry ->
                val args = backStackEntry.toRoute<NavigationRoute.EditProfile>()
                val editProfileViewModel: EditProfileViewModel = koinViewModel { parametersOf(args.userId) }
                EditProfileScreen(navController, editProfileViewModel, Modifier.padding(innerPadding))
            }

            composable<NavigationRoute.Welcome> {
                val welcomeViewModel: WelcomeViewModel = koinViewModel()
                WelcomeScreen(navController, welcomeViewModel)
            }

            composable<NavigationRoute.CreateProfile> {
                val createProfileViewModel: CreateProfileViewModel = koinViewModel()
                CreateProfileScreen(navController, createProfileViewModel)
            }

            composable<NavigationRoute.Login> {
                val loginViewModel: LoginViewModel = koinViewModel()
                LoginScreen(navController, loginViewModel)
            }
        }
    }
}
