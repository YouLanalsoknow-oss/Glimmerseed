package com.example.glimmerseed.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.glimmerseed.ui.dimens.AppDimens

enum class ScreenType {
    Phone, Tablet, WideScreen // 手机 / 平板 / 宽屏折叠屏
}

@Composable
fun rememberScreenType(): ScreenType {
    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp

    return when {
        screenWidthDp >= 840 -> ScreenType.WideScreen
        screenWidthDp >= 600 -> ScreenType.Tablet
        else -> ScreenType.Phone
    }
}

/** 根据屏幕类型获取适配后的时间轴尺寸 */
@Composable
fun getAdaptiveTimeAxisDimens(): TimeAxisDimens {
    return when (rememberScreenType()) {
        ScreenType.Phone -> TimeAxisDimens(
            rulerHeight = 90.dp,
            trackHeight = 40.dp,
            totalHeight = 180.dp,
            folderOffset = 18.dp
        )
        ScreenType.Tablet -> TimeAxisDimens(
            rulerHeight = 110.dp,
            trackHeight = 44.dp,
            totalHeight = 220.dp,
            folderOffset = 20.dp
        )
        ScreenType.WideScreen -> TimeAxisDimens(
            rulerHeight = AppDimens.TimeRulerHeight,
            trackHeight = AppDimens.TrackItemHeight,
            totalHeight = AppDimens.TimeAxisTotalHeight,
            folderOffset = AppDimens.TimeFolderLevelOffset
        )
    }
}

data class TimeAxisDimens(
    val rulerHeight: Dp,
    val trackHeight: Dp,
    val totalHeight: Dp,
    val folderOffset: Dp
)
