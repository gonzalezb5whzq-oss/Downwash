package com.velmar.othik.downwash

import android.app.Application
import com.velmar.othik.downwash.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DownwashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DownwashApp)
            modules(appModule)
        }
    }
}
