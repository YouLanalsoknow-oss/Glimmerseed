package com.example.glimmerseed.editorcore.panel

object BehaviorBoundaryGuard {

    private const val MAX_ACTION_CHAIN_LENGTH = 10
    private const val MAX_EVENTS_PER_PANEL = 50

    fun validateActionChain(actions: List<BehaviorAction>): ValidationResult {
        val violations = mutableListOf<BoundaryViolation>()

        checkActionChainLength(actions, violations)
        checkForbiddenActions(actions, violations)

        return if (violations.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(violations)
        }
    }

    fun validateEventCount(eventCount: Int): ValidationResult {
        return if (eventCount > MAX_EVENTS_PER_PANEL) {
            ValidationResult.Invalid(
                listOf(
                    BoundaryViolation(
                        type = ViolationType.EVENT_COUNT_EXCEEDED,
                        message = "Event count $eventCount exceeds limit $MAX_EVENTS_PER_PANEL"
                    )
                )
            )
        } else {
            ValidationResult.Valid
        }
    }

    private fun checkActionChainLength(actions: List<BehaviorAction>, violations: MutableList<BoundaryViolation>) {
        if (actions.size > MAX_ACTION_CHAIN_LENGTH) {
            violations.add(
                BoundaryViolation(
                    type = ViolationType.ACTION_CHAIN_TOO_LONG,
                    message = "Action chain length ${actions.size} exceeds limit $MAX_ACTION_CHAIN_LENGTH"
                )
            )
        }
    }

    private fun checkForbiddenActions(actions: List<BehaviorAction>, violations: MutableList<BoundaryViolation>) {
        actions.forEachIndexed { index, action ->
            when (action) {
                is BehaviorAction.SendEvent -> {
                    if (action.payload.size > 10) {
                        violations.add(
                            BoundaryViolation(
                                type = ViolationType.PAYLOAD_TOO_LARGE,
                                message = "Payload size ${action.payload.size} exceeds limit 10 at action index $index"
                            )
                        )
                    }
                }
                else -> {
                }
            }
        }
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val violations: List<BoundaryViolation>) : ValidationResult()
    }

    data class BoundaryViolation(
        val type: ViolationType,
        val message: String
    )

    enum class ViolationType {
        ACTION_CHAIN_TOO_LONG,
        EVENT_COUNT_EXCEEDED,
        PAYLOAD_TOO_LARGE
    }
}