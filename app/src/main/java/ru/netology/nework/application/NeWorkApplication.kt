package ru.netology.nework.application

import android.app.Application
import ru.netology.nework.BuildConfig
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeWorkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}