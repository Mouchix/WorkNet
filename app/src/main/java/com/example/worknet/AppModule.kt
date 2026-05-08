package com.example.worknet

import com.example.worknet.data.repository.ApplicationRepository
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.ui.home.HomeViewModel
import com.example.worknet.ui.place.PlaceDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { JobRepository() }

    single { PlaceRepository() }

    single { ApplicationRepository() }


    viewModel { HomeViewModel(get(), get()) }

    viewModel { params -> PlaceDetailViewModel(placeId = params.get(), get(), get(), get()) }
}