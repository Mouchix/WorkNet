package com.example.worknet.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.navigation.NavController
import com.example.worknet.navigation.NavigationRoute

@Composable
fun BottomBar(navController: NavController) {

    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(NavigationRoute.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(NavigationRoute.Favourites) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favourites") },
            label = { Text("Preferiti") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(NavigationRoute.Notifications) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
            label = { Text("Notifiche") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(NavigationRoute.Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profilo") }
        )
    }
}
