package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.glimmerseed.ui.dimens.AppDimens
import com.example.glimmerseed.ui.utils.TimeAxisDimens
import com.example.glimmerseed.ui.utils.getAdaptiveTimeAxisDimens

@Composable
fun CompleteFolderTimeAxis(
    state: TimeAxisState,
    tracks: List<List<Keyframe>>,
    modifier: Modifier = Modifier,
    onKeyframeSelected: (Int, Keyframe?) -> Unit = { _, _ -> },
    onKeyframeMoved: (Int, Keyframe, Float) -> Unit = { _, _, _ -> },
    uiScale: Float = 1f
) {
    val dimens = getAdaptiveTimeAxisDimens()
    Column(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy((AppDimens.TrackVerticalGap * uiScale))
    ) {
        FolderTimeAxis(
            state = state,
            dimens = dimens
        )

        tracks.forEachIndexed { index, keyframes ->
            KeyframeTrack(
                state = state,
                keyframes = keyframes,
                onKeyframeSelected = { onKeyframeSelected(index, it) },
                onKeyframeMoved = { kf, time -> onKeyframeMoved(index, kf, time) },
                dimens = dimens
            )
        }
    }
}
