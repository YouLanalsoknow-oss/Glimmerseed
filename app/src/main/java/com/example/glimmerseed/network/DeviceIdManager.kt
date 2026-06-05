package com.example.glimmerseed.network

import android.content.Context
import android.provider.Settings
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.deviceIdDataStore by preferencesDataStore(name = "device_id_manager")

object DeviceIdManager {
    private val KEY_DEVICE_ID = stringPreferencesKey("deviceId")

    @Volatile
    private var context: Context? = null
    private var cachedDeviceId: String? = null

    fun init(ctx: Context) {
        if (context == null) {
            context = ctx.applicationContext
        }
    }

    private fun requireContext(): Context {
        return context ?: error("DeviceIdManager not initialized! Call init() first.")
    }

    private val dataStore by lazy {
        requireContext().deviceIdDataStore
    }

    fun getOrCreate(ctx: Context): String {
        cachedDeviceId?.let { return it }

        return runBlocking {
            val savedId = dataStore.data.map { preferences ->
                preferences[KEY_DEVICE_ID]
            }.first()

            if (!savedId.isNullOrEmpty()) {
                cachedDeviceId = savedId
                return@runBlocking savedId
            }

            val newId = Settings.Secure.getString(
                ctx.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: java.util.UUID.randomUUID().toString()

            dataStore.edit { preferences ->
                preferences[KEY_DEVICE_ID] = newId
            }

            cachedDeviceId = newId
            newId
        }
    }
}