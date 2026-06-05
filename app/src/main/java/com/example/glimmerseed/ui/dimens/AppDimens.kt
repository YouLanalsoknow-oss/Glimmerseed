package com.example.glimmerseed.ui.dimens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 全局尺寸常量：统一管理所有组件宽、高、边距、间距、圆角
 * 按模块分类，便于维护
 */
object AppDimens {
    // region 基础通用间距（留白，解决拥挤核心）
    val SpaceTiny: Dp = 2.dp
    val SpaceSmall: Dp = 4.dp
    val SpaceNormal: Dp = 8.dp
    val SpaceMedium: Dp = 12.dp
    val SpaceLarge: Dp = 16.dp
    val SpaceHuge: Dp = 24.dp
    // endregion

    // region 圆角
    val RadiusSmall: Dp = 4.dp
    val RadiusNormal: Dp = 8.dp
    // endregion

    // region 分割线/线条宽度
    val LineThin: Dp = 1.dp
    val LineNormal: Dp = 2.dp
    // endregion

    // region 顶部工具栏
    val ToolbarHeight: Dp = 56.dp
    // endregion

    // region 左侧/右侧面板（横屏编辑器）
    val PanelWidthMin: Dp = 220.dp
    val PanelWidthDefault: Dp = 260.dp
    val PanelWidthMax: Dp = 320.dp
    // endregion

    // region 时间轴专属（重点：文件夹刻度 + 轨道）
    /** 文件夹式时间刻度整体高度 */
    val TimeRulerHeight: Dp = 120.dp
    /** 单条关键帧轨道高度 */
    val TrackItemHeight: Dp = 48.dp
    /** 轨道之间垂直间距 */
    val TrackVerticalGap: Dp = 6.dp
    /** 时间轴整体底部高度（包含标尺+多轨道） */
    val TimeAxisTotalHeight: Dp = 240.dp
    // 文件夹层级垂直偏移
    val TimeFolderLevelOffset: Dp = 22.dp
    // endregion

    // region 按钮/图标尺寸
    val IconSizeSmall: Dp = 16.dp
    val IconSizeNormal: Dp = 24.dp
    val ButtonHeightNormal: Dp = 40.dp
    // endregion

    // region 内边距（Padding）
    val PaddingContent: Dp = 12.dp
    val PaddingPanel: Dp = 16.dp
    // endregion
}
