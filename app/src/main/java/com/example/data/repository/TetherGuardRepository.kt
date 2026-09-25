package com.example.data.repository

import android.content.Context
import com.example.data.models.ActiveCommand
import com.example.data.models.CommandStatus
import com.example.data.models.ConnectionState
import com.example.data.models.GuardianState
import com.example.data.models.Incident
import com.example.data.models.IncidentResponseStatus
import com.example.data.models.IncidentType
import com.example.data.models.LaptopTelemetry
import com.example.data.models.PairedDevice
import com.example.data.models.QrPairingData
import com.example.data.storage.IncidentDao
import com.example.data.storage.IncidentEntity
import com.example.data.storage.SecureStorage
import com.example.data.storage.TetherGuardDatabase
import com.example.data.websocket.TetherRelaySimulator
import com.example.data.websocket.TetherWebSocketClient
import com.example.notifications.TetherNotificationManager
import com.example.security.auth.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TetherGuardRepository(
    private val context: Context,
    private val secureStorage: SecureStorage,
    private val database: TetherGuardDatabase,
    val sessionManager: SessionManager,
    val simulator: TetherRelaySimulator,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val incidentDao: IncidentDao = database.incidentDao()
    private val notificationManager = TetherNotificationManager(context)

    val webSocketClient: TetherWebSocketClient = TetherWebSocketClient(
        secureStorage = secureStorage,
        sessionManager = sessionManager,
        simulator = simulator,
        scope = scope
    )

    private val _pairedDevice = MutableStateFlow<PairedDevice?>(secureStorage.getPairedDevice())
    val pairedDevice: StateFlow<PairedDevice?> = _pairedDevice.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = webSocketClient.connectionState
    val telemetry: StateFlow<LaptopTelemetry> = webSocketClient.telemetry

    val guardianState: StateFlow<GuardianState> = telemetry.map { it.guardianState }
        .stateIn(scope, SharingStarted.Eagerly, GuardianState.ACTIVE)

    // Current active tamper incident alert (shown as high-visibility pop-up/banner)
    private val _activeIncidentAlert = MutableStateFlow<Incident?>(null)
    val activeIncidentAlert: StateFlow<Incident?> = _activeIncidentAlert.asStateFlow()

    // Command in flight or recently completed
    private val _currentCommand = MutableStateFlow<ActiveCommand?>(null)
    val currentCommand: StateFlow<ActiveCommand?> = _currentCommand.asStateFlow()

    val allIncidents: Flow<List<Incident>> = incidentDao.getAllIncidents().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val recentIncidents: Flow<List<Incident>> = incidentDao.getRecentIncidents(10).map { entities ->
        entities.map { it.toDomainModel() }
    }

    init {
        // Collect incoming incidents from live WebSocket / simulator
        scope.launch {
            webSocketClient.incomingIncidents.collect { incident ->
                // Insert into local persistent Room database
                val insertedId = incidentDao.insertIncident(IncidentEntity.fromDomain(incident))
                val savedIncident = incident.copy(id = insertedId)

                // Trigger high visibility alert
                _activeIncidentAlert.value = savedIncident

                // Send system notification
                notificationManager.showIncidentNotification(savedIncident)
            }
        }

        // Collect command acknowledgements
        scope.launch {
            webSocketClient.commandStatusUpdates.collect { activeCmd ->
                _currentCommand.value = activeCmd
            }
        }

        // Connect if already paired
        if (secureStorage.isPaired()) {
            webSocketClient.connect()
        }
    }

    fun isPaired(): Boolean = secureStorage.isPaired()

    fun pairDevice(qrData: QrPairingData): Result<Unit> {
        return try {
            if (!qrData.isValid()) {
                return Result.failure(IllegalArgumentException("Invalid QR payload fields"))
            }

            secureStorage.savePairing(
                deviceId = qrData.deviceId,
                deviceName = qrData.deviceName,
                publicKey = qrData.publicKey,
                relayEndpoint = qrData.relayEndpoint,
                secretToken = qrData.secretToken
            )

            _pairedDevice.value = secureStorage.getPairedDevice()
            webSocketClient.connect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun unpairDevice() {
        webSocketClient.disconnect()
        secureStorage.unpairDevice()
        _pairedDevice.value = null
        _activeIncidentAlert.value = null
        _currentCommand.value = null
    }

    fun enableGuardian() {
        sendCommand("GUARDIAN_ON")
    }

    fun disableGuardian() {
        sendCommand("GUARDIAN_OFF")
    }

    fun sendLockCommand() {
        sendCommand("LOCK")
    }

    fun sendShutdownCommand() {
        sendCommand("SHUTDOWN")
    }

    fun sendCommand(commandType: String) {
        webSocketClient.sendCommand(commandType) { activeCmd ->
            _currentCommand.value = activeCmd
        }
    }

    fun dismissActiveAlert() {
        _activeIncidentAlert.value = null
    }

    fun dismissCommandStatus() {
        _currentCommand.value = null
    }

    suspend fun resolveIncident(incidentId: Long, responseStatus: IncidentResponseStatus) {
        incidentDao.updateResponseStatus(
            id = incidentId,
            status = responseStatus.name,
            resolvedAt = System.currentTimeMillis()
        )
        if (_activeIncidentAlert.value?.id == incidentId) {
            _activeIncidentAlert.value = null
        }
    }

    suspend fun clearHistory() {
        incidentDao.clearAllIncidents()
    }

    fun triggerDemoIncident(type: IncidentType) {
        simulator.triggerSimulatedIncident(type)
    }

    companion object {
        @Volatile
        private var INSTANCE: TetherGuardRepository? = null

        fun getInstance(context: Context): TetherGuardRepository {
            return INSTANCE ?: synchronized(this) {
                val secureStorage = SecureStorage(context)
                val database = TetherGuardDatabase.getDatabase(context)
                val sessionManager = SessionManager(secureStorage)
                val simulator = TetherRelaySimulator()
                val instance = TetherGuardRepository(
                    context = context.applicationContext,
                    secureStorage = secureStorage,
                    database = database,
                    sessionManager = sessionManager,
                    simulator = simulator
                )
                INSTANCE = instance
                instance
            }
        }
    }
}
