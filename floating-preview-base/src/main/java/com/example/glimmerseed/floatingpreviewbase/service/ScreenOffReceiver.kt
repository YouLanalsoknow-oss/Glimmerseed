package com.example.glimmerseed.floatingpreviewbase.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

internal class ScreenOffReceiver(private val service: StageService) : BroadcastReceiver() {

    private var isRegistered = false

    fun register() {
        if (isRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        service.registerReceiver(this, filter)
        isRegistered = true
    }

    fun unregister() {
        if (!isRegistered) return
        try {
            service.unregisterReceiver(this)
        } catch (_: Exception) {
        }
        isRegistered = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> service.panelManager?.onScreenOff()
            Intent.ACTION_SCREEN_ON -> service.panelManager?.onScreenOn()
        }
    }
}