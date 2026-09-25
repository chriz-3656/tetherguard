package com.example.data.models

enum class GuardianState {
    UNKNOWN,
    OFF,
    ARMING,
    ACTIVE,
    TRIGGERED,
    DISARMING;

    val isArmed: Boolean
        get() = this == ACTIVE || this == TRIGGERED

    val label: String
        get() = when (this) {
            UNKNOWN -> "UNKNOWN"
            OFF -> "STANDBY"
            ARMING -> "ARMING..."
            ACTIVE -> "ACTIVE"
            TRIGGERED -> "ALERT TRIGGERED"
            DISARMING -> "DISARMING..."
        }
}
