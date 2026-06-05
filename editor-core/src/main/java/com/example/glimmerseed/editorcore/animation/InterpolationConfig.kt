package com.example.glimmerseed.editorcore.animation

import kotlinx.serialization.Serializable

@Serializable
sealed class InterpolationConfig {

    @Serializable
    enum class PresetType {
        SMOOTH,
        RUSH_IN,
        RUSH_OUT,
        BOUNCE,
        OVERSHOOT,
        SPRING,
        LINEAR,
        STEPPED
    }

    @Serializable
    data class Preset(
        val type: PresetType,
        val elasticity: Float = 0.5f,
        val velocity: Float = 0.5f,
        val bias: Float = 0.0f
    ) : InterpolationConfig() {
        init {
            require(elasticity in 0f..1f) { "elasticity must be between 0 and 1" }
            require(velocity in 0f..1f) { "velocity must be between 0 and 1" }
            require(bias in -1f..1f) { "bias must be between -1 and 1" }
        }

        override fun evaluate(progress: Float): Float {
            return when (type) {
                PresetType.SMOOTH -> easeInOutCubic(progress, elasticity, velocity, bias)
                PresetType.RUSH_IN -> easeInCubic(progress, velocity)
                PresetType.RUSH_OUT -> easeOutCubic(progress, velocity)
                PresetType.BOUNCE -> bounce(progress, elasticity, velocity)
                PresetType.OVERSHOOT -> overshoot(progress, elasticity, velocity)
                PresetType.SPRING -> springApproximation(progress, elasticity, velocity)
                PresetType.LINEAR -> progress
                PresetType.STEPPED -> if (progress >= 1f) 1f else 0f
            }
        }

        override fun toBezier(): Bezier {
            return when (type) {
                PresetType.SMOOTH -> Bezier(0.42f, 0f, 0.58f, 1f)
                PresetType.RUSH_IN -> Bezier(0.55f, 0.055f, 0.675f, 0.19f)
                PresetType.RUSH_OUT -> Bezier(0.215f, 0.61f, 0.355f, 1f)
                PresetType.BOUNCE -> Bezier(0.68f, -0.55f, 0.265f, 1.55f)
                PresetType.OVERSHOOT -> Bezier(0.6f, -0.28f, 0.735f, 0.045f)
                PresetType.SPRING -> Bezier(0.25f, 0.1f, 0.25f, 1f)
                PresetType.LINEAR -> Bezier(0f, 0f, 1f, 1f)
                PresetType.STEPPED -> Bezier(1f, 0f, 1f, 0f)
            }
        }

        private fun easeInOutCubic(t: Float, elasticity: Float, velocity: Float, bias: Float): Float {
            val adjustedT = when {
                bias < 0 -> easeInCubic(t, (1 - bias).toFloat())
                bias > 0 -> easeOutCubic(t, (1 + bias).toFloat())
                else -> t
            }

            val base = if (adjustedT < 0.5f) {
                4f * adjustedT * adjustedT * adjustedT
            } else {
                1f - Math.pow((-2f * adjustedT + 2f).toDouble(), 3.0).toFloat() / 2f
            }

            return base + (bounce(t, elasticity, velocity) - t) * elasticity * 0.3f
        }

        private fun easeInCubic(t: Float, velocity: Float): Float {
            val v = 0.5f + velocity * 0.5f
            return Math.pow(t.toDouble(), (1 + v * 2f).toDouble()).toFloat()
        }

        private fun easeOutCubic(t: Float, velocity: Float): Float {
            val v = 0.5f + velocity * 0.5f
            return 1f - Math.pow((1f - t).toDouble(), (1 + v * 2f).toDouble()).toFloat()
        }

        private fun bounce(t: Float, elasticity: Float, velocity: Float): Float {
            val b = 4f * elasticity + 4f
            val c = 1f - elasticity * 0.3f

            return when {
                t < 1f / b -> b * b * t * t
                t < 2f / b -> b * b * (t - 1.5f / b) * (t - 1.5f / b) + c
                t < 2.5f / b -> b * b * (t - 2.25f / b) * (t - 2.25f / b) + c
                else -> b * b * (t - 2.625f / b) * (t - 2.625f / b) + c
            }
        }

        private fun overshoot(t: Float, elasticity: Float, velocity: Float): Float {
            val p = elasticity * 0.3f
            val s = p / 4f

            return if (t < 1f) {
                t * t * ((p + 1f) * t - p)
            } else {
                (t - 1f) * (t - 1f) * ((p + 1f) * (t - 1f) + p) + 1f
            }
        }

        private fun springApproximation(t: Float, elasticity: Float, velocity: Float): Float {
            val omega = 8f + elasticity * 8f
            val zeta = 0.6f - elasticity * 0.3f

            if (t >= 1f) return 1f

            val envelope = kotlin.math.exp((-zeta * omega * t).toDouble()).toFloat()
            return 1f - envelope * kotlin.math.cos((omega * kotlin.math.sqrt((1f - zeta * zeta).toDouble()) * t).toDouble()).toFloat()
        }
    }

    @Serializable
    data class Bezier(
        val p1x: Float,
        val p1y: Float,
        val p2x: Float,
        val p2y: Float
    ) : InterpolationConfig() {
        init {
            require(p1x in 0f..1f) { "p1x must be between 0 and 1" }
            require(p2x in 0f..1f) { "p2x must be between 0 and 1" }
        }

        override fun evaluate(progress: Float): Float {
            var t = progress
            var x: Float
            var d: Float

            for (i in 0..8) {
                x = 3f * p1x * t * (1f - t) * (1f - t) + 3f * p2x * t * t * (1f - t) + t * t * t
                d = 3f * p1x * (1f - t) * (1f - t) + 6f * p2x * t * (1f - t) + 3f * t * t
                if (d < 1e-6f) break
                val t1 = t - (x - progress) / d
                if (t1 == t) break
                t = t1
            }

            return 3f * p1y * t * (1f - t) * (1f - t) + 3f * p2y * t * t * (1f - t) + t * t * t
        }

        override fun toBezier(): Bezier {
            return this
        }
    }

    @Serializable
    data class Spring(
        val stiffness: Float,
        val damping: Float,
        val mass: Float = 1.0f
    ) : InterpolationConfig() {
        init {
            require(stiffness > 0f) { "stiffness must be positive" }
            require(damping >= 0f) { "damping must be non-negative" }
            require(mass > 0f) { "mass must be positive" }
        }

        override fun evaluate(progress: Float): Float {
            if (progress >= 1f) return 1f

            val omega0 = kotlin.math.sqrt((stiffness / mass).toDouble()).toFloat()
            val zeta = (damping / (2f * kotlin.math.sqrt((stiffness * mass).toDouble()).toFloat())).toFloat()
            val omegaD = omega0 * kotlin.math.sqrt((1f - zeta * zeta).toDouble()).toFloat()
            val t = progress * omega0

            if (zeta < 1f) {
                val envelope = kotlin.math.exp((-zeta * t).toDouble()).toFloat()
                val cosTerm = kotlin.math.cos((omegaD * t).toDouble()).toFloat()
                val sinTerm = kotlin.math.sin((omegaD * t).toDouble()).toFloat()
                return 1f - envelope * (cosTerm + zeta / kotlin.math.sqrt((1f - zeta * zeta).toDouble()).toFloat() * sinTerm)
            } else {
                val envelope = kotlin.math.exp((-t).toDouble()).toFloat()
                return 1f - envelope * (1f + t)
            }
        }

        override fun toBezier(): Bezier {
            return Bezier(0.25f, 0.1f, 0.25f, 1f)
        }
    }

    @Serializable
    object Slerp : InterpolationConfig() {
        override fun evaluate(progress: Float): Float {
            return progress
        }

        override fun toBezier(): Bezier {
            return Bezier(0f, 0f, 1f, 1f)
        }
    }

    abstract fun evaluate(progress: Float): Float

    abstract fun toBezier(): Bezier
}
