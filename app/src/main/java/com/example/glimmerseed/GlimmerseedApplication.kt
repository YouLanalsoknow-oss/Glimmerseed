package com.example.glimmerseed

import android.app.Application
import com.example.glimmerseed.app.data.SettingsDataStore
import com.example.glimmerseed.data.asset.AppAssetManager
import com.example.glimmerseed.editorcore.asset.AssetManager
import com.example.glimmerseed.network.DeviceIdManager
import timber.log.Timber

class GlimmerseedApplication : Application() {

    lateinit var assetManager: AssetManager
        private set

    override fun onCreate() {
        super.onCreate()

        assetManager = AppAssetManager(this)

        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }

        SettingsDataStore.init(this)
        DeviceIdManager.init(this)
        DeviceIdManager.getOrCreate(this)
    }
}
