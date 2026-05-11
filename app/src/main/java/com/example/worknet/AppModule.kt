package com.example.worknet

import com.example.worknet.data.repository.ApplicationRepository
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.NotificationRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.UserRepository
import com.example.worknet.ui.components.bottomBar.BottomBarViewModel
import com.example.worknet.ui.favourites.FavouritesViewModel
import com.example.worknet.ui.home.HomeViewModel
import com.example.worknet.ui.notifications.NotificationsViewModel
import com.example.worknet.ui.place.PlaceDetailViewModel
import com.example.worknet.ui.profile.addplace.AddPlaceViewModel
import com.example.worknet.ui.profile.ProfileViewModel
import com.example.worknet.ui.profile.myPlaces.MyPlacesViewModel
import com.example.worknet.ui.profile.UserViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { JobRepository() }

    single { PlaceRepository() }

    single { ApplicationRepository() }

    single { UserRepository() }

    single { NotificationRepository() }

    viewModel { HomeViewModel(get(), get()) }

    viewModel { params -> PlaceDetailViewModel(placeId = params.get(), get(), get(), get(), get(), get()) }

    viewModel { ProfileViewModel(get()) }

    viewModel { FavouritesViewModel(get(), get(), get()) }

    viewModel { NotificationsViewModel(get(), get()) }

    viewModel { AddPlaceViewModel(get(), get(), get()) }

    viewModel { params -> UserViewModel(params.get(),get(), get(), get()) }

    viewModel { BottomBarViewModel(get(), get()) }

    viewModel { MyPlacesViewModel(get(), get(), get()) }
}