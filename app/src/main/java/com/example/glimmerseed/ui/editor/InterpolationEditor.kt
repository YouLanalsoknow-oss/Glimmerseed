package com.example.glimmerseed.ui.editor

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.editorcore.animation.InterpolationConfig
import com.example.glimmerseed.editorcore.animation.InterpolationConfig.PresetType
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.GlimmerseedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterpolationEditor(
    config: InterpolationConfig,
    onConfigChange: (InterpolationConfig) -> Unit
) {
    val expandedState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PreviewAnimation(config = config)

        PresetSelector(
            config = config,
            onPresetChange = { preset ->
                onConfigChange(preset)
            }
        )

        ParameterSliders(
            config = config,
            onParameterChange = { elasticity, velocity, bias ->
                if (config is InterpolationConfig.Preset) {
                    onConfigChange(config.copy(
                        elasticity = elasticity,
                        velocity = velocity,
                        bias = bias
                    ))
                }
            }
        )

        AdvancedSection(
            config = config,
            expanded = expandedState.value,
            onExpandedChange = { expandedState.value = it },
            onConfigChange = onConfigChange
        )
    }
}

@Composable
fun PreviewAnimation(config: InterpolationConfig) {
    val infiniteTransition = rememberInfiniteTransition()
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    ).value

    val easedProgress = remember(progress) {
        config.evaluate(progress)
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val trackHeight = 4.dp.toPx()
                val trackY = height / 2

                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(30f, trackY),
                    end = Offset(width - 30f, trackY),
                    strokeWidth = trackHeight
                )

                drawLine(
                    color = Color(0xFF9E9E9E),
                    start = Offset(30f, trackY - 8f),
                    end = Offset(30f, trackY + 8f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF9E9E9E),
                    start = Offset(width - 30f, trackY - 8f),
                    end = Offset(width - 30f, trackY + 8f),
                    strokeWidth = 2f
                )

                val ballX = 30f + (width - 60f) * easedProgress
                val ballY = trackY
                val ballRadius = 16f

                drawCircle(
                    color = primaryColor,
                    center = Offset(ballX, ballY),
                    radius = ballRadius
                )

                drawCircle(
                    color = Color.White,
                    center = Offset(ballX - ballRadius * 0.3f, ballY - ballRadius * 0.3f),
                    radius = ballRadius * 0.3f
                )
            }
        }
    }
}

@Composable
fun PresetSelector(
    config: InterpolationConfig,
    onPresetChange: (InterpolationConfig.Preset) -> Unit
) {
    val currentPreset = when (config) {
        is InterpolationConfig.Preset -> config.type
        else -> null
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "预设",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetType.values().forEach { type ->
                PresetButton(
                    type = type,
                    selected = currentPreset == type,
                    onClick = { onPresetChange(InterpolationConfig.Preset(type)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PresetButton(
    type: PresetType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ParameterSliders(
    config: InterpolationConfig,
    onParameterChange: (elasticity: Float, velocity: Float, bias: Float) -> Unit
) {
    val preset = config as? InterpolationConfig.Preset ?: return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "参数调节",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ParameterSlider(
            label = "弹性",
            value = preset.elasticity,
            range = 0f..1f,
            onValueChange = { onParameterChange(it, preset.velocity, preset.bias) }
        )

        ParameterSlider(
            label = "速度",
            value = preset.velocity,
            range = 0f..1f,
            onValueChange = { onParameterChange(preset.elasticity, it, preset.bias) }
        )

        ParameterSlider(
            label = "偏向",
            value = preset.bias,
            range = -1f..1f,
            onValueChange = { onParameterChange(preset.elasticity, preset.velocity, it) }
        )
    }
}

@Composable
fun ParameterSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )

        Text(
            text = "%.2f".format(value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Right
        )
    }
}

@Composable
fun AdvancedSection(
    config: InterpolationConfig,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onConfigChange: (InterpolationConfig) -> Unit
) {
    Button(
        onClick = { onExpandedChange(!expanded) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "高级",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = if (expanded) AppIcons.ArrowDropUp else AppIcons.ArrowDropDown,
                contentDescription = if (expanded) "收起" else "展开"
            )
        }
    }

    if (expanded) {
        Column(modifier = Modifier.padding(12.dp)) {
            when (config) {
                is InterpolationConfig.Preset -> {
                    val bezier = config.toBezier()
                    BezierInput(
                        bezier = bezier,
                        onBezierChange = { onConfigChange(it) }
                    )

                    if (config.type == PresetType.SPRING) {
                        SpringInput(onSpringChange = { onConfigChange(it) })
                    }
                }
                is InterpolationConfig.Bezier -> {
                    BezierInput(
                        bezier = config,
                        onBezierChange = { onConfigChange(it) }
                    )
                }
                is InterpolationConfig.Spring -> {
                    SpringInput(
                        stiffness = config.stiffness,
                        damping = config.damping,
                        mass = config.mass,
                        onSpringChange = { onConfigChange(it) }
                    )
                }
                InterpolationConfig.Slerp -> {
                    Text(
                        text = "SLERP - 旋转专用插值",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BezierInput(
    bezier: InterpolationConfig.Bezier,
    onBezierChange: (InterpolationConfig.Bezier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "贝塞尔控制点",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BezierInputField(
                label = "P1x",
                value = bezier.p1x,
                onValueChange = { onBezierChange(bezier.copy(p1x = it)) }
            )
            BezierInputField(
                label = "P1y",
                value = bezier.p1y,
                onValueChange = { onBezierChange(bezier.copy(p1y = it)) }
            )
            BezierInputField(
                label = "P2x",
                value = bezier.p2x,
                onValueChange = { onBezierChange(bezier.copy(p2x = it)) }
            )
            BezierInputField(
                label = "P2y",
                value = bezier.p2y,
                onValueChange = { onBezierChange(bezier.copy(p2y = it)) }
            )
        }
    }
}

@Composable
fun BezierInputField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        GlimmerseedTextField(
            value = "%.2f".format(value),
            onValueChange = { onValueChange(it.toFloatOrNull() ?: 0f) },
            modifier = Modifier.width(60.dp),
            singleLine = true
        )
    }
}

@Composable
fun SpringInput(
    stiffness: Float = 100f,
    damping: Float = 10f,
    mass: Float = 1f,
    onSpringChange: (InterpolationConfig.Spring) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "弹簧物理参数",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ParameterSlider(
            label = "刚度",
            value = stiffness / 200f,
            range = 0f..1f,
            onValueChange = { onSpringChange(InterpolationConfig.Spring(it * 200f, damping, mass)) }
        )

        ParameterSlider(
            label = "阻尼",
            value = damping / 50f,
            range = 0f..1f,
            onValueChange = { onSpringChange(InterpolationConfig.Spring(stiffness, it * 50f, mass)) }
        )

        ParameterSlider(
            label = "质量",
            value = (mass - 0.1f) / 1.9f,
            range = 0f..1f,
            onValueChange = { onSpringChange(InterpolationConfig.Spring(stiffness, damping, 0.1f + it * 1.9f)) }
        )
    }
}

val PresetType.displayName: String
    get() = when (this) {
        PresetType.SMOOTH -> "平滑"
        PresetType.RUSH_IN -> "冲入"
        PresetType.RUSH_OUT -> "冲出"
        PresetType.BOUNCE -> "弹跳"
        PresetType.OVERSHOOT -> "回弹"
        PresetType.SPRING -> "弹簧"
        PresetType.LINEAR -> "线性"
        PresetType.STEPPED -> "阶跃"
    }
