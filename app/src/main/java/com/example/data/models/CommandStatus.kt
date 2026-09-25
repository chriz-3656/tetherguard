package com.example.data.models

enum class CommandStatus {
    IDLE,
    SENDING,
    ACKNOWLEDGED,
    FAILED,
    TIMEOUT;

    val isInFlight: Boolean
        get() = this == SENDING
}

data class ActiveCommand(
    val requestId: String,
    val command: String,
    val status: CommandStatus,
    val sentAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
