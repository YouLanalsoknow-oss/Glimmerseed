package com.example.glimmerseed.ui.common

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val Add = createAddIcon()
    val Delete = createDeleteIcon()
    val Check = createCheckIcon()
    val Close = createCloseIcon()
    val Search = createSearchIcon()
    val List = createListIcon()
    val Minimize = createMinimizeIcon()
    val LineStyle = createLineStyleIcon()
    val ArrowDownward = createArrowDownwardIcon()
    val ArrowUpward = createArrowUpwardIcon()
    val Waves = createWavesIcon()
    val Square = createSquareIcon()
    val ArrowDropUp = createArrowDropUpIcon()
    val ArrowDropDown = createArrowDropDownIcon()
    val ArrowDropRight = createArrowDropRightIcon()
    val Timeline = createTimelineIcon()
    val TrendingFlat = createTrendingFlatIcon()
    val TrendingDown = createTrendingDownIcon()
    val TrendingUp = createTrendingUpIcon()
    val Bounce = createBounceIcon()
    val Folder = createFolderIcon()
    val FolderOpen = createFolderOpenIcon()
    val Image = createImageIcon()
    val Photo = createPhotoIcon()
    val Person = createPersonIcon()
    val Create = createCreateIcon()
    val Refresh = createRefreshIcon()
    val Save = createSaveIcon()
    val Menu = createMenuIcon()
    val MoreVert = createMoreVertIcon()
    val KeyboardArrowUp = createKeyboardArrowUpIcon()
    val KeyboardArrowDown = createKeyboardArrowDownIcon()
    val KeyboardArrowRight = createKeyboardArrowRightIcon()
    val Share = createShareIcon()
    val Home = createHomeIcon()
    val Send = createSendIcon()
    val Star = createStarIcon()
    val Info = createInfoIcon()
    val Settings = createSettingsIcon()
    val Edit = createEditIcon()
    val ArrowForward = createArrowForwardIcon()
    val Undo = createUndoIcon()
    val Redo = createRedoIcon()
    val Grid = createGridIcon()
    val OnionSkin = createOnionSkinIcon()
    val Skin = createSkinIcon()
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

private fun createAddIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12f, 5f)
            lineTo(12f, 19f)
            moveTo(5f, 12f)
            lineTo(19f, 12f)
        }
    }.build()
}

private fun createDeleteIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(19f, 6f)
            lineTo(5f, 6f)
            moveTo(12f, 19f)
            lineTo(12f, 10f)
            moveTo(8f, 10f)
            lineTo(16f, 10f)
        }
    }.build()
}

private fun createCheckIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(20f, 6f)
            lineTo(9f, 17f)
            lineTo(4f, 12f)
        }
    }.build()
}

private fun createCloseIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(18f, 6f)
            lineTo(6f, 18f)
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }
    }.build()
}

private fun createSearchIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        group {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            ) {
                moveTo(19f, 19f)
                lineTo(14f, 14f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            ) {
                arcToRelative(9f, 9f, 0f, true, true, 0f, 2f)
                arcToRelative(9f, 9f, 0f, true, true, 0f, -2f)
            }
        }
    }.build()
}

private fun createListIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(8f, 6f)
            lineTo(21f, 6f)
            moveTo(8f, 12f)
            lineTo(21f, 12f)
            moveTo(8f, 18f)
            lineTo(21f, 18f)
            moveTo(3f, 6f)
            lineTo(3.01f, 6f)
            moveTo(3f, 12f)
            lineTo(3.01f, 12f)
            moveTo(3f, 18f)
            lineTo(3.01f, 18f)
        }
    }.build()
}

private fun createMinimizeIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(6f, 19f)
            lineTo(6f, 12f)
            lineTo(18f, 12f)
        }
    }.build()
}

private fun createLineStyleIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12f, 20f)
            lineTo(20f, 12f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            close()
        }
    }.build()
}

private fun createArrowDownwardIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12f, 5f)
            lineTo(12f, 19f)
            moveTo(19f, 12f)
            lineTo(5f, 12f)
            moveTo(12f, 5f)
            lineTo(5f, 12f)
            moveTo(12f, 5f)
            lineTo(19f, 12f)
        }
    }.build()
}

private fun createArrowUpwardIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12f, 19f)
            lineTo(12f, 5f)
            moveTo(19f, 12f)
            lineTo(5f, 12f)
            moveTo(12f, 19f)
            lineTo(5f, 12f)
            moveTo(12f, 19f)
            lineTo(19f, 12f)
        }
    }.build()
}

private fun createWavesIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(2f, 12f)
            curveTo(6f, 8f, 10f, 16f, 14f, 12f)
            curveTo(18f, 8f, 22f, 16f, 22f, 12f)
        }
    }.build()
}

private fun createSquareIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(3f, 3f)
            lineTo(21f, 3f)
            lineTo(21f, 21f)
            lineTo(3f, 21f)
            close()
        }
    }.build()
}

private fun createArrowDropUpIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(7f, 14f)
            lineTo(12f, 8f)
            lineTo(17f, 14f)
            close()
        }
    }.build()
}

private fun createArrowDropDownIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(7f, 10f)
            lineTo(12f, 16f)
            lineTo(17f, 10f)
            close()
        }
    }.build()
}

private fun createArrowDropRightIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(10f, 7f)
            lineTo(16f, 12f)
            lineTo(10f, 17f)
            close()
        }
    }.build()
}

private fun createTimelineIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(2f, 12f)
            lineTo(22f, 12f)
            moveTo(10f, 2f)
            lineTo(10f, 22f)
            moveTo(14f, 2f)
            lineTo(14f, 22f)
        }
    }.build()
}

private fun createTrendingFlatIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(2f, 12f)
            lineTo(22f, 12f)
        }
    }.build()
}

private fun createTrendingDownIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(2f, 3f)
            lineTo(22f, 21f)
            moveTo(10f, 21f)
            lineTo(10f, 3f)
        }
    }.build()
}

private fun createTrendingUpIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(2f, 21f)
            lineTo(22f, 3f)
            moveTo(10f, 3f)
            lineTo(10f, 21f)
        }
    }.build()
}

private fun createBounceIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12f, 2f)
            curveTo(8f, 6f, 6f, 12f, 12f, 22f)
            curveTo(18f, 12f, 16f, 6f, 12f, 2f)
        }
    }.build()
}

private fun createFolderIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(10f, 4f)
            lineTo(2f, 4f)
            lineTo(2f, 20f)
            lineTo(22f, 20f)
            lineTo(22f, 8f)
            lineTo(14f, 8f)
            lineTo(10f, 4f)
            close()
        }
    }.build()
}

private fun createImageIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(2f, 16f)
            lineTo(2f, 22f)
            lineTo(22f, 22f)
            lineTo(22f, 16f)
            close()
            moveTo(15f, 2f)
            curveTo(18f, 2f, 21f, 5f, 21f, 8f)
            curveTo(21f, 11f, 18f, 14f, 15f, 14f)
            curveTo(12f, 14f, 9f, 11f, 9f, 8f)
            curveTo(9f, 5f, 12f, 2f, 15f, 2f)
            close()
            moveTo(7f, 8f)
            lineTo(13f, 14f)
            moveTo(13f, 8f)
            verticalLineTo(14f)
        }
    }.build()
}

private fun createPhotoIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(23f, 19f)
            lineTo(16f, 12f)
            lineTo(5f, 21f)
            close()
            moveTo(16f, 3f)
            lineTo(21f, 3f)
            lineTo(21f, 8f)
            close()
        }
    }.build()
}

private fun createPersonIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            arcToRelative(7f, 7f, 0f, true, false, 0f, 2f)
            arcToRelative(7f, 7f, 0f, true, false, 0f, -2f)
            moveTo(16f, 19f)
            lineTo(16f, 21f)
            lineTo(8f, 21f)
            lineTo(8f, 19f)
        }
    }.build()
}

private fun createCreateIcon(): ImageVector {
    return createAddIcon()
}

private fun createRefreshIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(23f, 12f)
            moveTo(17f, 3f)
            arcToRelative(7f, 7f, 0f, true, false, -11.898f, 7.243f)
            moveTo(16f, 3f)
            lineTo(16f, 9f)
            lineTo(22f, 9f)
        }
    }.build()
}

private fun createFolderOpenIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(3f, 7f)
            lineTo(3f, 18f)
            lineTo(21f, 18f)
            lineTo(21f, 9f)
            lineTo(11f, 9f)
            lineTo(9f, 7f)
            close()
        }
    }.build()
}

private fun createSaveIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(19f, 21f)
            lineTo(5f, 21f)
            arcToRelative(2f, 2f, 0f, false, true, 3f, 19f)
            lineTo(3f, 5f)
            arcToRelative(2f, 2f, 0f, false, true, 5f, 3f)
            lineTo(17f, 3f)
            lineTo(21f, 7f)
            lineTo(21f, 19f)
            arcToRelative(2f, 2f, 0f, false, true, 19f, 21f)
            moveTo(17f, 21f)
            lineTo(17f, 13f)
            lineTo(7f, 13f)
            lineTo(7f, 21f)
            moveTo(7f, 10f)
            lineTo(7f, 7f)
            lineTo(15f, 7f)
            lineTo(15f, 10f)
        }
    }.build()
}

private fun createMenuIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(3f, 6f)
            lineTo(21f, 6f)
            moveTo(3f, 12f)
            lineTo(21f, 12f)
            moveTo(3f, 18f)
            lineTo(21f, 18f)
        }
    }.build()
}

private fun createMoreVertIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(12f, 8f)
            arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
            arcToRelative(2f, 2f, 0f, true, false, 0f, 4f)
            close()
            moveTo(12f, 14f)
            arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
            arcToRelative(2f, 2f, 0f, true, false, 0f, 4f)
            close()
            moveTo(12f, 20f)
            arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
            arcToRelative(2f, 2f, 0f, true, false, 0f, 4f)
            close()
        }
    }.build()
}

private fun createKeyboardArrowUpIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(7f, 14f)
            lineTo(12f, 9f)
            lineTo(17f, 14f)
        }
    }.build()
}

private fun createKeyboardArrowDownIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(7f, 10f)
            lineTo(12f, 15f)
            lineTo(17f, 10f)
        }
    }.build()
}

private fun createKeyboardArrowRightIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(8f, 5f)
            lineTo(14f, 12f)
            lineTo(8f, 19f)
        }
    }.build()
}

private fun createShareIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(4f, 12f)
            verticalLineToRelative(8f)
            arcTo(2f, 2f, 0f, false, false, 2f, 2f)
            horizontalLineToRelative(12f)
            arcTo(2f, 2f, 0f, false, false, 2f, -2f)
            verticalLineToRelative(-8f)
            moveTo(16f, 6f)
            lineTo(12f, 2f)
            lineTo(8f, 6f)
            moveTo(12f, 2f)
            verticalLineToRelative(12f)
        }
    }.build()
}

private fun createHomeIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(10f, 20f)
            verticalLineToRelative(-6f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(5f)
            verticalLineToRelative(-10f)
            lineTo(12f, 3f)
            lineTo(2f, 10f)
            verticalLineToRelative(10f)
            close()
        }
    }.build()
}

private fun createSendIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(22f, 2f)
            lineTo(11f, 13f)
            moveTo(22f, 2f)
            lineTo(15f, 22f)
            lineTo(11f, 13f)
            lineTo(2f, 9f)
            lineTo(22f, 2f)
        }
    }.build()
}

private fun createStarIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(12f, 2f)
            lineTo(15.09f, 8.26f)
            lineTo(22f, 9.27f)
            lineTo(17f, 14.14f)
            lineTo(18.18f, 21.02f)
            lineTo(12f, 18f)
            lineTo(5.82f, 21.02f)
            lineTo(7f, 14.14f)
            lineTo(2f, 9.27f)
            lineTo(8.91f, 8.26f)
            close()
        }
    }.build()
}

private fun createInfoIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, true, false, 0f, 20f)
            arcTo(10f, 10f, 0f, true, false, 12f, 2f)
            close()
            moveTo(12f, 8f)
            verticalLineToRelative(0.01f)
            moveTo(12f, 16f)
            verticalLineToRelative(-4f)
        }
    }.build()
}

private fun createSettingsIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12.22f, 2f)
            horizontalLineToRelative(-0.44f)
            curveToRelative(-0.63f, 0f, -1.23f, 0.29f, -1.62f, 0.79f)
            lineTo(9.58f, 3.68f)
            curveToRelative(-0.25f, 0.31f, -0.62f, 0.51f, -1.03f, 0.56f)
            lineTo(7.69f, 4.38f)
            curveToRelative(-0.62f, 0.08f, -1.18f, 0.43f, -1.54f, 0.95f)
            lineTo(5.47f, 6.66f)
            curveToRelative(-0.26f, 0.37f, -0.65f, 0.64f, -1.09f, 0.76f)
            lineTo(3.36f, 7.78f)
            curveToRelative(-0.6f, 0.17f, -1.08f, 0.61f, -1.3f, 1.19f)
            lineTo(1.72f, 11f)
            curveToRelative(-0.15f, 0.41f, -0.15f, 0.86f, 0f, 1.27f)
            lineToRelative(0.34f, 1.03f)
            curveToRelative(0.22f, 0.58f, 0.7f, 1.02f, 1.3f, 1.19f)
            lineToRelative(1.02f, 0.28f)
            curveToRelative(0.44f, 0.12f, 0.83f, 0.39f, 1.09f, 0.76f)
            lineToRelative(0.68f, 0.97f)
            curveToRelative(0.36f, 0.52f, 0.92f, 0.87f, 1.54f, 0.95f)
            lineToRelative(0.86f, 0.11f)
            curveToRelative(0.41f, 0.05f, 0.78f, 0.25f, 1.03f, 0.56f)
            lineToRelative(0.58f, 0.73f)
            curveToRelative(0.39f, 0.5f, 0.99f, 0.79f, 1.62f, 0.79f)
            horizontalLineToRelative(0.44f)
            curveToRelative(0.63f, 0f, 1.23f, -0.29f, 1.62f, -0.79f)
            lineToRelative(0.58f, -0.73f)
            curveToRelative(0.25f, -0.31f, 0.62f, -0.51f, 1.03f, -0.56f)
            lineToRelative(0.86f, -0.11f)
            curveToRelative(0.62f, -0.08f, 1.18f, -0.43f, 1.54f, -0.95f)
            lineToRelative(0.68f, -0.97f)
            curveToRelative(0.26f, -0.37f, 0.65f, -0.64f, 1.09f, -0.76f)
            lineToRelative(1.02f, -0.28f)
            curveToRelative(0.6f, -0.17f, 1.08f, -0.61f, 1.3f, -1.19f)
            lineToRelative(0.34f, -1.03f)
            curveToRelative(0.15f, -0.41f, 0.15f, -0.86f, 0f, -1.27f)
            lineToRelative(-0.34f, -1.03f)
            curveToRelative(-0.22f, -0.58f, -0.7f, -1.02f, -1.3f, -1.19f)
            lineTo(20.64f, 7.42f)
            curveToRelative(-0.44f, -0.12f, -0.83f, -0.39f, -1.09f, -0.76f)
            lineToRelative(-0.68f, -0.97f)
            curveToRelative(-0.36f, -0.52f, -0.92f, -0.87f, -1.54f, -0.95f)
            lineToRelative(-0.86f, -0.11f)
            curveToRelative(-0.41f, -0.05f, -0.78f, -0.25f, -1.03f, -0.56f)
            lineToRelative(-0.58f, -0.73f)
            curveTo(13.45f, 2.29f, 12.85f, 2f, 12.22f, 2f)
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12f, 16f)
            arcTo(4f, 4f, 0f, true, false, 0f, -8f)
            arcTo(4f, 4f, 0f, true, false, 12f, 16f)
        }
    }.build()
}

private fun createEditIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(17f, 3f)
            arcToRelative(2.83f, 2.83f, 0f, true, true, 4f, 4f)
            lineTo(7.5f, 20.5f)
            lineTo(2f, 22f)
            lineTo(3.5f, 16.5f)
            close()
        }
    }.build()
}

private fun createArrowForwardIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(5f, 12f)
            horizontalLineToRelative(14f)
            moveTo(12f, 5f)
            lineTo(19f, 12f)
            lineTo(12f, 19f)
        }
    }.build()
}

private fun createUndoIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round) {
            moveTo(12.5f, 8f)
            curveTo(16.09f, 8f, 19f, 10.91f, 19f, 14.5f)
            curveTo(19f, 18.09f, 16.09f, 21f, 12.5f, 21f)
            moveTo(3f, 21f)
            lineTo(7f, 14.5f)
            lineTo(3f, 8f)
        }
    }.build()
}

private fun createRedoIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round) {
            moveTo(11.5f, 8f)
            curveTo(7.91f, 8f, 5f, 10.91f, 5f, 14.5f)
            curveTo(5f, 18.09f, 7.91f, 21f, 11.5f, 21f)
            moveTo(21f, 21f)
            lineTo(17f, 14.5f)
            lineTo(21f, 8f)
        }
    }.build()
}

private fun createGridIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f) {
            moveTo(4f, 4f); lineTo(20f, 4f)
            moveTo(4f, 12f); lineTo(20f, 12f)
            moveTo(4f, 20f); lineTo(20f, 20f)
            moveTo(4f, 4f); lineTo(4f, 20f)
            moveTo(12f, 4f); lineTo(12f, 20f)
            moveTo(20f, 4f); lineTo(20f, 20f)
        }
    }.build()
}

private fun createOnionSkinIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round) {
            // 三层圆环表示洋葱皮
            arcTo(9f, 9f, 0f, false, true, 3f, 15f)
            arcTo(6f, 6f, 0f, false, true, 6f, 14f)
            arcTo(3f, 3f, 0f, false, true, 9f, 13f)
        }
    }.build()
}

private fun createSkinIcon(): ImageVector {
    return ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        // 简单的蒙皮图标：一个带填充的区域轮廓
        path(fill = SolidColor(Color.Black), stroke = null) {
            moveTo(4f, 4f)
            lineTo(6f, 2f)
            lineTo(18f, 2f)
            lineTo(20f, 4f)
            lineTo(20f, 17f)
            lineTo(18f, 19f)
            lineTo(6f, 19f)
            lineTo(4f, 17f)
            close()
        }
        path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 1.5f) {
            moveTo(7f, 6f); lineTo(17f, 6f)
            moveTo(7f, 11f); lineTo(17f, 11f)
            moveTo(7f, 16f); lineTo(13f, 16f)
        }
    }.build()
}