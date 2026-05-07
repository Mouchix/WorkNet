package com.example.worknet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.worknet.ui.home.Home
import com.example.worknet.ui.theme.WorkNetTheme
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkNetTheme {
                /*Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }*/
                Home()
            }
        }
    }
}

sealed interface NavigationRoute {
    @Serializable data object Screen1 : NavigationRoute
    @Serializable data object Screen2 : NavigationRoute
    @Serializable data object Screen3 : NavigationRoute
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Screen1
    ) {
        composable<NavigationRoute.Screen1> {
            Screen1(navController)
        }
        composable<NavigationRoute.Screen2> {
            Screen2(navController)
        }
        composable<NavigationRoute.Screen3> {
            Screen3(navController)
        }
    }
}