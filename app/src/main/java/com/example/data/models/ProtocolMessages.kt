package com.example.data.models

data class OutgoingCommandMessage(
    val type: String = "COMMAND",
    val command: String,
    val device_id: String,
    val request_id: String,
    val signature: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class OutgoingAuthMessage(
    val type: String = "REGISTER_MOBILE",
    val device_id: String,
    val auth_token: String,
    val client_id: String = "android_tetherguard_v1"
)

data class LaptopTelemetry(
    val deviceId: String,
    val guardianState: GuardianState = GuardianState.ACTIVE,
    val usbMonitoring: Boolean = true,
    val inputMonitoring: Boolean = true,
    val webcamWatch: Boolean = true,
    val lidSensor: Boolean = true,
    val isWorkstationLocked: Boolean = false,
    val batteryPct: Int = 92,
    val lastHeartbeatTime: Long = System.currentTimeMillis()
)
