package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.Incident
import com.example.data.models.IncidentResponseStatus
import com.example.data.models.IncidentSeverity
import com.example.data.models.IncidentType
import com.example.data.storage.IncidentEntity
import com.example.data.storage.SecureStorage
import com.example.pairing.QrParser
import com.example.security.auth.SessionManager
import com.example.security.crypto.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TetherGuardSecurityTest {

    private lateinit var context: Context
    private lateinit var secureStorage: SecureStorage
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        secureStorage = SecureStorage(context)
        sessionManager = SessionManager(secureStorage)
    }

    @Test
    fun testValidQrPayloadParsing() {
        val json = """
            {
              "device_id": "TG-8842",
              "device_name": "CHRIZ-LAPTOP",
              "public_key": "pk_ed25519_9912",
              "nonce": "a8f3b9c0",
              "relay_endpoint": "wss://relay.tetherguard.sec/ws",
              "secret_token": "tg_secret_token_123"
            }
        """.trimIndent()

        val result = QrParser.parse(json)
        assertTrue(result.isSuccess)
        val data = result.getOrNull()
        assertNotNull(data)
        assertEquals("TG-8842", data?.deviceId)
        assertEquals("CHRIZ-LAPTOP", data?.deviceName)
        assertEquals("wss://relay.tetherguard.sec/ws", data?.relayEndpoint)
        assertTrue(data?.isValid() == true)
    }

    @Test
    fun testInvalidQrPayloadRejected() {
        // Missing device_id
        val missingDeviceId = """
            {
              "device_name": "CHRIZ-LAPTOP",
              "relay_endpoint": "wss://relay.tetherguard.sec/ws"
            }
        """.trimIndent()
        val result1 = QrParser.parse(missingDeviceId)
        assertFalse(result1.isSuccess)

        // Invalid non-websocket scheme
        val invalidScheme = """
            {
              "device_id": "TG-8842",
              "device_name": "CHRIZ-LAPTOP",
              "relay_endpoint": "http://insecure.endpoint.com"
            }
        """.trimIndent()
        val result2 = QrParser.parse(invalidScheme)
        assertFalse(result2.isSuccess)

        // Empty string
        val result3 = QrParser.parse("")
        assertFalse(result3.isSuccess)
    }

    @Test
    fun testDeviceIdentityValidation() {
        secureStorage.savePairing(
            deviceId = "TG-8842",
            deviceName = "CHRIZ-LAPTOP",
            publicKey = "pk_test",
            relayEndpoint = "wss://relay.tetherguard.sec/ws",
            secretToken = "token_xyz"
        )

        // Same device ID should validate
        assertTrue(sessionManager.validateDevice("TG-8842"))
        assertTrue(sessionManager.validateDevice("tg-8842")) // case-insensitive

        // Impostor or different device ID must be rejected
        assertFalse(sessionManager.validateDevice("TG-MALICIOUS"))
        assertFalse(sessionManager.validateDevice(null))
        assertFalse(sessionManager.validateDevice(""))
    }

    @Test
    fun testAuthenticatedCommandConstruction() {
        secureStorage.savePairing(
            deviceId = "TG-8842",
            deviceName = "CHRIZ-LAPTOP",
            publicKey = "pk_test",
            relayEndpoint = "wss://relay.tetherguard.sec/ws",
            secretToken = "token_xyz"
        )

        val lockResult = sessionManager.buildAuthenticatedCommand("LOCK")
        assertTrue(lockResult.isSuccess)
        val lockCmd = lockResult.getOrThrow()
        assertEquals("LOCK", lockCmd.command)
        assertEquals("TG-8842", lockCmd.device_id)
        assertNotNull(lockCmd.request_id)
        assertNotNull(lockCmd.signature)
        assertTrue(lockCmd.timestamp > 0)
    }

    @Test
    fun testShutdownCooldownPrevention() {
        secureStorage.savePairing(
            deviceId = "TG-8842",
            deviceName = "CHRIZ-LAPTOP",
            publicKey = "pk_test",
            relayEndpoint = "wss://relay.tetherguard.sec/ws",
            secretToken = "token_xyz"
        )

        // First shutdown command should succeed
        val firstShutdown = sessionManager.buildAuthenticatedCommand("SHUTDOWN")
        assertTrue(firstShutdown.isSuccess)

        // Immediate subsequent shutdown command MUST be rejected by cooldown
        val secondShutdown = sessionManager.buildAuthenticatedCommand("SHUTDOWN")
        assertFalse(secondShutdown.isSuccess)
        assertTrue(secondShutdown.exceptionOrNull()?.message?.contains("cooldown") == true)
    }

    @Test
    fun testIncidentEntityConversion() {
        val incident = Incident(
            id = 42L,
            eventId = "INC-1001",
            eventType = IncidentType.USB_INSERT,
            deviceId = "TG-8842",
            timestamp = "14:32:07",
            timestampMillis = 1700000000000L,
            severity = IncidentSeverity.HIGH,
            deviceName = "USB Mass Storage",
            vendorId = "0x0781",
            productId = "0x5583",
            evidenceAvailable = true,
            evidenceImageBase64 = "sample_base64",
            responseStatus = IncidentResponseStatus.LOCKED
        )

        val entity = IncidentEntity.fromDomain(incident)
        assertEquals(42L, entity.id)
        assertEquals("USB_INSERT", entity.eventType)
        assertEquals("HIGH", entity.severity)

        val convertedBack = entity.toDomainModel()
        assertEquals(incident.id, convertedBack.id)
        assertEquals(incident.eventType, convertedBack.eventType)
        assertEquals(incident.severity, convertedBack.severity)
        assertEquals(incident.deviceName, convertedBack.deviceName)
        assertEquals(IncidentResponseStatus.LOCKED, convertedBack.responseStatus)
    }

    @Test
    fun testSecureStorageUnpairWipesCredentials() {
        secureStorage.savePairing(
            deviceId = "TG-8842",
            deviceName = "CHRIZ-LAPTOP",
            publicKey = "pk_test",
            relayEndpoint = "wss://relay.tetherguard.sec/ws",
            secretToken = "token_xyz"
        )

        assertTrue(secureStorage.isPaired())
        assertEquals("TG-8842", secureStorage.getDeviceId())

        // Execute secure wipe
        secureStorage.unpairDevice()

        assertFalse(secureStorage.isPaired())
        assertEquals(null, secureStorage.getDeviceId())
        assertEquals(null, secureStorage.getPairedDevice())
    }
}
