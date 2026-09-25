package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.models.ActiveCommand
import com.example.data.models.ConnectionState
import com.example.data.models.GuardianState
import com.example.data.models.Incident
import com.example.data.models.IncidentResponseStatus
import com.example.data.models.IncidentType
import com.example.data.models.LaptopTelemetry
import com.example.data.models.PairedDevice
import com.example.data.repository.TetherGuardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val pairedDevice: PairedDevice? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val guardianState: GuardianState = GuardianState.ACTIVE,
    val telemetry: LaptopTelemetry = LaptopTelemetry("TG-8842"),
    val lastIncident: Incident? = null,
    val activeIncidentAlert: Incident? = null,
    val activeCommand: ActiveCommand? = null
)

class DashboardViewModel(
    private val repository: TetherGuardRepository
) : ViewModel() {

    val pairedDevice: StateFlow<PairedDevice?> = repository.pairedDevice
    val connectionState: StateFlow<ConnectionState> = repository.connectionState
    val guardianState: StateFlow<GuardianState> = repository.guardianState
    val telemetry: StateFlow<LaptopTelemetry> = repository.telemetry
    val activeIncidentAlert: StateFlow<Incident?> = repository.activeIncidentAlert
    val activeCommand: StateFlow<ActiveCommand?> = repository.currentCommand

    val lastIncident: StateFlow<Incident?> = repository.recentIncidents.map { list ->
        list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allIncidents: StateFlow<List<Incident>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun enableGuardian() {
        repository.enableGuardian()
    }

    fun disableGuardian() {
        repository.disableGuardian()
    }

    fun sendLockCommand() {
        repository.sendLockCommand()
    }

    fun sendShutdownCommand() {
        repository.sendShutdownCommand()
    }

    fun dismissCommandBanner() {
        repository.dismissCommandStatus()
    }

    fun dismissActiveAlert() {
        repository.dismissActiveAlert()
    }

    fun keepLockedOnAlert(incident: Incident) {
        viewModelScope.launch {
            repository.sendLockCommand()
            repository.resolveIncident(incident.id, IncidentResponseStatus.LOCKED)
        }
    }

    fun remoteShutdownOnAlert(incident: Incident) {
        viewModelScope.launch {
            repository.sendShutdownCommand()
            repository.resolveIncident(incident.id, IncidentResponseStatus.SHUTDOWN)
        }
    }

    fun triggerDemoIncident(type: IncidentType) {
        repository.triggerDemoIncident(type)
    }

    fun reconnect() {
        repository.webSocketClient.connect()
    }

    class Factory(private val repository: TetherGuardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
