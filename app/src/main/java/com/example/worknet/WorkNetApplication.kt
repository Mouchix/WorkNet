package com.example.worknet

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WorkNetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WorkNetApp)
            modules(appModule)
        }
    }
}