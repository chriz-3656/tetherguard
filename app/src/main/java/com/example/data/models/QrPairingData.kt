package com.example.data.models

data class QrPairingData(
    val deviceId: String,
    val deviceName: String,
    val publicKey: String,
    val nonce: String,
    val relayEndpoint: String,
    val secretToken: String,
    val encryptionStandard: String = "AES-256-GCM"
) {
    fun isValid(): Boolean {
        return deviceId.isNotBlank() &&
                device_name_is_valid() &&
                relayEndpoint.startsWith("ws://") || relayEndpoint.startsWith("wss://")
    }

    private fun device_name_is_valid(): Boolean = deviceName.isNotBlank()
}

data class PairedDevice(
    val deviceId: String,
    val deviceName: String,
    val publicKey: String,
    val relayEndpoint: String,
    val pairedAt: Long = System.currentTimeMillis()
)
