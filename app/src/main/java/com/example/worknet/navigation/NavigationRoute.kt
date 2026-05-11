package com.example.worknet.navigation

import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    @Serializable data object Home : NavigationRoute
    @Serializable data object Favourites : NavigationRoute
    @Serializable data object Notifications : NavigationRoute
    @Serializable data object Profile : NavigationRoute
    @Serializable data class PlaceDetail(val placeId: String) : NavigationRoute
    @Serializable data object AddPlace : NavigationRoute
    @Serializable data class User(val userId: String) : NavigationRoute
    @Serializable data object MyPlaces : NavigationRoute
    @Serializable data class EditProfile(val userId: String) : NavigationRoute
}
