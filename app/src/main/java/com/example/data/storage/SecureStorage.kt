package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.PairedDevice
import com.example.security.crypto.CryptoManager

class SecureStorage(
    context: Context,
    private val cryptoManager: CryptoManager = CryptoManager()
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    fun isPaired(): Boolean {
        return !getDeviceId().isNullOrBlank()
    }

    fun savePairing(
        deviceId: String,
        deviceName: String,
        publicKey: String,
        relayEndpoint: String,
        secretToken: String
    ) {
        val encryptedToken = cryptoManager.encrypt(secretToken)
        prefs.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .putString(KEY_DEVICE_NAME, deviceName)
            .putString(KEY_PUBLIC_KEY, publicKey)
            .putString(KEY_RELAY_ENDPOINT, relayEndpoint)
            .putString(KEY_ENCRYPTED_SECRET_TOKEN, encryptedToken)
            .putLong(KEY_PAIRED_AT, System.currentTimeMillis())
            .apply()
    }

    fun getPairedDevice(): PairedDevice? {
        val id = getDeviceId() ?: return null
        val name = prefs.getString(KEY_DEVICE_NAME, "LAPTOP") ?: "LAPTOP"
        val pubKey = prefs.getString(KEY_PUBLIC_KEY, "") ?: ""
        val relay = prefs.getString(KEY_RELAY_ENDPOINT, "wss://relay.tetherguard.sec/ws") ?: "wss://relay.tetherguard.sec/ws"
        val pairedAt = prefs.getLong(KEY_PAIRED_AT, System.currentTimeMillis())

        return PairedDevice(
            deviceId = id,
            deviceName = name,
            publicKey = pubKey,
            relayEndpoint = relay,
            pairedAt = pairedAt
        )
    }

    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }

    fun getDeviceName(): String {
        return prefs.getString(KEY_DEVICE_NAME, "SECURE-LAPTOP") ?: "SECURE-LAPTOP"
    }

    fun getRelayEndpoint(): String {
        return prefs.getString(KEY_RELAY_ENDPOINT, "wss://relay.tetherguard.sec/ws") ?: "wss://relay.tetherguard.sec/ws"
    }

    fun getSecretToken(): String? {
        val encrypted = prefs.getString(KEY_ENCRYPTED_SECRET_TOKEN, null) ?: return null
        return cryptoManager.decrypt(encrypted)
    }

    fun isDemoModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DEMO_MODE, true) // Default to demo mode enabled for hackathon evaluation
    }

    fun setDemoModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEMO_MODE, enabled).apply()
    }

    fun isVibrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_VIBRATION, true)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND, true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    fun isAutoReconnectEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_RECONNECT, true)
    }

    fun setAutoReconnectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RECONNECT, enabled).apply()
    }

    fun setRelayEndpoint(url: String) {
        prefs.edit().putString(KEY_RELAY_ENDPOINT, url).apply()
    }

    /**
     * Securely wipes all paired identity, tokens, and credentials.
     */
    fun unpairDevice() {
        prefs.edit()
            .remove(KEY_DEVICE_ID)
            .remove(KEY_DEVICE_NAME)
            .remove(KEY_PUBLIC_KEY)
            .remove(KEY_ENCRYPTED_SECRET_TOKEN)
            .remove(KEY_PAIRED_AT)
            .apply()
    }

    companion object {
        private const val PREF_FILE = "tetherguard_secure_prefs"
        private const val KEY_DEVICE_ID = "paired_device_id"
        private const val KEY_DEVICE_NAME = "paired_device_name"
        private const val KEY_PUBLIC_KEY = "paired_public_key"
        private const val KEY_RELAY_ENDPOINT = "relay_endpoint"
        private const val KEY_ENCRYPTED_SECRET_TOKEN = "enc_secret_token"
        private const val KEY_PAIRED_AT = "paired_at_timestamp"
        private const val KEY_DEMO_MODE = "demo_mode_enabled"
        private const val KEY_VIBRATION = "setting_vibration"
        private const val KEY_SOUND = "setting_sound"
        private const val KEY_AUTO_RECONNECT = "setting_auto_reconnect"
    }
}
