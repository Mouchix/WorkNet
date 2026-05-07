package com.example.worknet.navigation

import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    @Serializable data object Home : NavigationRoute
    @Serializable data object Favourites : NavigationRoute
    @Serializable data object Notifications : NavigationRoute
    @Serializable data object Profile : NavigationRoute
}
