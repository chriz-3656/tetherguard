package com.example.security.auth

import com.example.data.models.ActiveCommand
import com.example.data.models.CommandStatus
import com.example.data.models.OutgoingCommandMessage
import com.example.data.storage.SecureStorage
import com.example.security.crypto.CryptoManager
import java.util.UUID

class SessionManager(
    private val secureStorage: SecureStorage,
    private val cryptoManager: CryptoManager = CryptoManager()
) {
    // Guard against accidental rapid repeated commands (e.g. SHUTDOWN)
    private var lastShutdownSentAt: Long = 0L
    private val shutdownCooldownMs = 10_000L // 10 second cooldown on SHUTDOWN commands

    /**
     * Validates whether an incoming event or message matches our paired device ID.
     */
    fun validateDevice(incomingDeviceId: String?): Boolean {
        val pairedId = secureStorage.getDeviceId() ?: return false
        if (incomingDeviceId.isNullOrBlank()) return false
        return pairedId.equals(incomingDeviceId.trim(), ignoreCase = true)
    }

    /**
     * Prepares an authenticated OutgoingCommandMessage with UUID request_id and HMAC signature.
     */
    fun buildAuthenticatedCommand(commandType: String): Result<OutgoingCommandMessage> {
        val deviceId = secureStorage.getDeviceId()
            ?: return Result.failure(IllegalStateException("No device paired"))

        // Prevent accidental rapid repeated SHUTDOWN requests
        if (commandType.equals("SHUTDOWN", ignoreCase = true)) {
            val now = System.currentTimeMillis()
            if (now - lastShutdownSentAt < shutdownCooldownMs) {
                val remainingSec = ((shutdownCooldownMs - (now - lastShutdownSentAt)) / 1000) + 1
                return Result.failure(IllegalStateException("Shutdown cooldown active. Wait $remainingSec seconds."))
            }
            lastShutdownSentAt = now
        }

        val requestId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val secretToken = secureStorage.getSecretToken() ?: "tg_default_key"

        val payloadToSign = "$commandType:$deviceId:$requestId:$timestamp"
        val signature = cryptoManager.signCommand(payloadToSign, secretToken)

        val message = OutgoingCommandMessage(
            type = "COMMAND",
            command = commandType.uppercase(),
            device_id = deviceId,
            request_id = requestId,
            signature = signature,
            timestamp = timestamp
        )

        return Result.success(message)
    }

    fun canSendShutdown(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastShutdownSentAt) >= shutdownCooldownMs
    }
}
