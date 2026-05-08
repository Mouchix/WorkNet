package com.example.worknet

import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.ui.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { JobRepository() }

    single { PlaceRepository() }

    viewModel { HomeViewModel(get(), get()) }
}