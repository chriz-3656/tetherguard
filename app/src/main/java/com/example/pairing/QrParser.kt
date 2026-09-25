package com.example.pairing

import android.net.Uri
import com.example.data.models.QrPairingData
import org.json.JSONObject

object QrParser {

    /**
     * Parses raw QR payload content (supports JSON format and URI scheme).
     * Validates that device_id, device_name, and relay_endpoint are present and valid.
     */
    fun parse(rawPayload: String): Result<QrPairingData> {
        val trimmed = rawPayload.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Empty QR payload"))
        }

        // Case 1: JSON payload
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return try {
                val json = JSONObject(trimmed)
                val deviceId = json.optString("device_id").ifEmpty { json.optString("deviceId") }
                val deviceName = json.optString("device_name").ifEmpty {
                    json.optString("deviceName", "SECURE-WORKSTATION")
                }
                val publicKey = json.optString("public_key").ifEmpty {
                    json.optString("publicKey", "ed25519_pk_default")
                }
                val nonce = json.optString("nonce", "nonce_${System.currentTimeMillis()}")
                val relayEndpoint = json.optString("relay_endpoint").ifEmpty {
                    json.optString("relayEndpoint", "wss://relay.tetherguard.sec/ws")
                }
                val secretToken = json.optString("secret_token").ifEmpty {
                    json.optString("secretToken", "tg_token_${System.currentTimeMillis()}")
                }

                if (deviceId.isBlank()) {
                    return Result.failure(IllegalArgumentException("Missing required 'device_id' in QR payload"))
                }
                if (!relayEndpoint.startsWith("ws://") && !relayEndpoint.startsWith("wss://")) {
                    return Result.failure(IllegalArgumentException("Invalid relay endpoint: Must start with ws:// or wss://"))
                }

                Result.success(
                    QrPairingData(
                        deviceId = deviceId,
                        deviceName = deviceName,
                        publicKey = publicKey,
                        nonce = nonce,
                        relayEndpoint = relayEndpoint,
                        secretToken = secretToken
                    )
                )
            } catch (e: Exception) {
                Result.failure(IllegalArgumentException("Malformed JSON QR payload: ${e.message}"))
            }
        }

        // Case 2: Custom URI scheme tetherguard://pair?...
        if (trimmed.startsWith("tetherguard://") || trimmed.startsWith("tg://")) {
            return try {
                val uri = Uri.parse(trimmed)
                val deviceId = uri.getQueryParameter("device_id") ?: uri.getQueryParameter("id") ?: ""
                val deviceName = uri.getQueryParameter("device_name") ?: uri.getQueryParameter("name") ?: "SECURE-WORKSTATION"
                val publicKey = uri.getQueryParameter("public_key") ?: "ed25519_pk_default"
                val nonce = uri.getQueryParameter("nonce") ?: "nonce_${System.currentTimeMillis()}"
                val relay = uri.getQueryParameter("relay") ?: uri.getQueryParameter("relay_endpoint") ?: "wss://relay.tetherguard.sec/ws"
                val token = uri.getQueryParameter("token") ?: uri.getQueryParameter("secret_token") ?: "tg_token_uri"

                if (deviceId.isBlank()) {
                    return Result.failure(IllegalArgumentException("Missing device_id parameter in URI"))
                }

                Result.success(
                    QrPairingData(
                        deviceId = deviceId,
                        deviceName = deviceName,
                        publicKey = publicKey,
                        nonce = nonce,
                        relayEndpoint = relay,
                        secretToken = token
                    )
                )
            } catch (e: Exception) {
                Result.failure(IllegalArgumentException("Invalid URI QR format: ${e.message}"))
            }
        }

        return Result.failure(IllegalArgumentException("Unsupported QR format. Expected TetherGuard JSON or tetherguard:// URI"))
    }
}
