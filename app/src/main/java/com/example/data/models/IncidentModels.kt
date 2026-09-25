package com.example.data.models

enum class IncidentSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    companion object {
        fun fromString(value: String?): IncidentSeverity {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class IncidentType(val displayName: String) {
    USB_INSERT("UNEXPECTED USB DEVICE"),
    LID_OPEN("LAPTOP LID OPENED"),
    INPUT_ATTEMPT("UNAUTHORIZED INPUT ATTEMPT"),
    SUSPICIOUS_DEVICE("SUSPICIOUS PERIPHERAL"),
    PERIMETER_MOTION("MOTION DETECTED"),
    POWER_DISCONNECT("AC POWER DISCONNECTED"),
    NETWORK_DISCONNECT("NETWORK INTERFACE TAMPER");

    companion object {
        fun fromString(value: String?): IncidentType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: USB_INSERT
        }
    }
}

enum class IncidentResponseStatus {
    PENDING,
    LOCKED,
    SHUTDOWN,
    ACKNOWLEDGED,
    RESOLVED;

    val displayLabel: String
        get() = when (this) {
            PENDING -> "Action Required"
            LOCKED -> "Workstation Locked"
            SHUTDOWN -> "Workstation Terminated"
            ACKNOWLEDGED -> "Event Acknowledged"
            RESOLVED -> "Resolved & Cleared"
        }
}

data class Incident(
    val id: Long = 0L,
    val eventId: String,
    val eventType: IncidentType,
    val deviceId: String,
    val timestamp: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val severity: IncidentSeverity,
    val deviceName: String,
    val vendorId: String? = null,
    val productId: String? = null,
    val additionalDetails: String? = null,
    val evidenceAvailable: Boolean = false,
    val evidenceImageBase64: String? = null,
    val responseStatus: IncidentResponseStatus = IncidentResponseStatus.PENDING,
    val resolvedAt: Long? = null
)
