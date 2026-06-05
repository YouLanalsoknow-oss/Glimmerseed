package com.example.glimmerseed.floatingpreviewbase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 悬浮窗权限管理器
 * 负责管理SYSTEM_ALERT_WINDOW权限的请求、检测和状态通知
 */
class FloatingWindowPermissionManager(
    private val context: Context
) {

    /**
     * 权限状态
     */
    sealed class PermissionState {
        /** 未检查状态 */
        object Unknown : PermissionState()
        /** 已授权 */
        object Granted : PermissionState()
        /** 未授权 */
        object Denied : PermissionState()
    }

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Unknown)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    /**
     * 检查悬浮窗权限是否已授予
     */
    fun checkPermission(): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        _permissionState.value = if (hasPermission) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }

        return hasPermission
    }

    /**
     * 跳转到系统权限请求页面
     */
    fun requestPermission(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return null
    }

    /**
     * 刷新权限状态
     * 建议在Activity的onResume中调用
     */
    fun refreshPermissionState() {
        checkPermission()
    }

    /**
     * 初始化权限管理器
     * 在Application或Activity启动时调用
     */
    fun initialize() {
        checkPermission()
    }
}
