package com.example.data.websocket

import com.example.data.models.GuardianState
import com.example.data.models.Incident
import com.example.data.models.IncidentResponseStatus
import com.example.data.models.IncidentSeverity
import com.example.data.models.IncidentType
import com.example.data.models.LaptopTelemetry
import com.example.data.models.OutgoingCommandMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Embedded Mock Relay Engine for safe hackathon testing and demo presentations.
 * Emulates the TetherGuard laptop daemon communicating over a bidirectional WebSocket.
 */
class TetherRelaySimulator(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _telemetry = MutableStateFlow(
        LaptopTelemetry(
            deviceId = "TG-8842",
            guardianState = GuardianState.ACTIVE,
            usbMonitoring = true,
            inputMonitoring = true,
            webcamWatch = true,
            lidSensor = true,
            isWorkstationLocked = false,
            batteryPct = 89
        )
    )
    val telemetry: StateFlow<LaptopTelemetry> = _telemetry.asStateFlow()

    private val _simulatedIncomingFrames = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val simulatedIncomingFrames: SharedFlow<String> = _simulatedIncomingFrames.asSharedFlow()

    fun updateDeviceId(deviceId: String) {
        _telemetry.value = _telemetry.value.copy(deviceId = deviceId)
    }

    /**
     * Handles outgoing commands and generates realistic ACKs and state changes.
     */
    fun processCommand(commandMessage: OutgoingCommandMessage) {
        scope.launch {
            delay(350) // Realistic network round-trip delay

            when (commandMessage.command.uppercase()) {
                "GUARDIAN_ON" -> {
                    _telemetry.value = _telemetry.value.copy(guardianState = GuardianState.ACTIVE)
                    sendAck(commandMessage.request_id, "SUCCESS", "Guardian Mode is now ACTIVE")
                    broadcastStatus()
                }
                "GUARDIAN_OFF" -> {
                    _telemetry.value = _telemetry.value.copy(guardianState = GuardianState.OFF)
                    sendAck(commandMessage.request_id, "SUCCESS", "Guardian Mode set to STANDBY")
                    broadcastStatus()
                }
                "LOCK" -> {
                    _telemetry.value = _telemetry.value.copy(isWorkstationLocked = true)
                    sendAck(commandMessage.request_id, "SUCCESS", "Workstation locked and display dimmed")
                    broadcastStatus()
                }
                "SHUTDOWN" -> {
                    _telemetry.value = _telemetry.value.copy(
                        guardianState = GuardianState.OFF,
                        isWorkstationLocked = true
                    )
                    sendAck(commandMessage.request_id, "SUCCESS", "ACPI shutdown sequence initiated safely")
                    broadcastStatus()
                }
                "PING" -> {
                    sendPong()
                }
                else -> {
                    sendAck(commandMessage.request_id, "UNKNOWN_COMMAND", "Command ${commandMessage.command} not recognized")
                }
            }
        }
    }

    private suspend fun sendAck(requestId: String, status: String, message: String) {
        val json = JSONObject().apply {
            put("type", "ACK")
            put("request_id", requestId)
            put("status", status)
            put("message", message)
            put("timestamp", System.currentTimeMillis())
        }
        _simulatedIncomingFrames.emit(json.toString())
    }

    private suspend fun sendPong() {
        val json = JSONObject().apply {
            put("type", "PONG")
            put("timestamp", System.currentTimeMillis())
        }
        _simulatedIncomingFrames.emit(json.toString())
    }

    private suspend fun broadcastStatus() {
        val current = _telemetry.value
        val json = JSONObject().apply {
            put("type", "STATUS")
            put("device_id", current.deviceId)
            put("guardian_state", current.guardianState.name)
            put("usb_monitoring", current.usbMonitoring)
            put("input_monitoring", current.inputMonitoring)
            put("webcam_watch", current.webcamWatch)
            put("lid_sensor", current.lidSensor)
            put("workstation_locked", current.isWorkstationLocked)
            put("battery_pct", current.batteryPct)
            put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        }
        _simulatedIncomingFrames.emit(json.toString())
    }

    /**
     * Injects a simulated physical tamper incident (e.g. USB insert, Lid open, Keylogger device).
     */
    fun triggerSimulatedIncident(
        type: IncidentType = IncidentType.USB_INSERT,
        severity: IncidentSeverity = IncidentSeverity.HIGH
    ) {
        scope.launch {
            val deviceId = _telemetry.value.deviceId
            val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

            val (devName, vid, pid) = when (type) {
                IncidentType.USB_INSERT -> Triple("USB Mass Storage (SanDisk Ultra)", "0x0781", "0x5583")
                IncidentType.LID_OPEN -> Triple("Hall-Effect Lid Angle Sensor", "0x0001", "0x0042")
                IncidentType.INPUT_ATTEMPT -> Triple("Virtual Keystroke Injector (RubberDucky)", "0x16c0", "0x05df")
                IncidentType.SUSPICIOUS_DEVICE -> Triple("Unknown PCIe / Thunderbolt Device", "0x8086", "0x15d9")
                IncidentType.PERIMETER_MOTION -> Triple("Integrated FaceTime HD Camera Sensor", "0x05ac", "0x8514")
                IncidentType.POWER_DISCONNECT -> Triple("MagSafe / AC Power Supply", "0x05ac", "0x0002")
                IncidentType.NETWORK_DISCONNECT -> Triple("Realtek Gigabit Ethernet Controller", "0x10ec", "0x8168")
            }

            // Laptop daemon locks itself upon detecting tamper
            _telemetry.value = _telemetry.value.copy(
                guardianState = GuardianState.TRIGGERED,
                isWorkstationLocked = true
            )

            // Evidence image: high contrast tactical vector bitmap representation (Base64)
            // A compact sample 1x1 or tactical placeholder base64
            val sampleEvidenceBase64 = SAMPLE_SECURITY_CAMERA_FRAME_BASE64

            val json = JSONObject().apply {
                put("type", "INCIDENT")
                put("event", type.name)
                put("device_id", deviceId)
                put("timestamp", isoTime)
                put("severity", severity.name)

                val meta = JSONObject().apply {
                    put("device_name", devName)
                    put("vendor_id", vid)
                    put("product_id", pid)
                    put("subsystem", "IOKit / udev-guard")
                    put("threat_score", if (severity == IncidentSeverity.CRITICAL) 95 else 78)
                }
                put("metadata", meta)

                val evidence = JSONObject().apply {
                    put("available", true)
                    put("image", sampleEvidenceBase64)
                }
                put("evidence", evidence)
            }

            _simulatedIncomingFrames.emit(json.toString())
        }
    }

    companion object {
        // Small 1x1 transparent PNG / tactical test base64
        const val SAMPLE_SECURITY_CAMERA_FRAME_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    }
}
